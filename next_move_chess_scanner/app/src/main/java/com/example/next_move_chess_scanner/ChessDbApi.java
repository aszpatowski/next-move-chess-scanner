package com.example.next_move_chess_scanner;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class ChessDbApi{
    private static List<Move> movesList = new ArrayList<>();
    private static final String baseUrl = "https://www.chessdb.cn/cdb.php?action=queryall&board=";
    private static final String returnJson = "&json=1";
    private static Context context;
    private static final String Tag = "CHESSDBAPI";
    private int maxResults;

    ChessDbApi(Context ctx, int maxResults) {
        context = ctx;
        this.maxResults = maxResults;
    }

// Sends a GET request to a server using the given FEN notation
// and returns a List of Move objects
public List<Move> sendRequest(String fenNotation) {
    /*
     Sends a GET request to a server using the given FEN notation
    and returns a List of Move objects
     */

    // Build the URL to send the request to
    String completedURL = baseUrl + fenNotation + returnJson;
    
    // Create a RequestQueue to handle the request
    RequestQueue queue = Volley.newRequestQueue(this.context);
    
    // Create a JsonObjectRequest to get a JSON response from the server
    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, completedURL,null,
            new Response.Listener<JSONObject>() {
                @Override
                // Handle the JSON response received from the server
                public void onResponse(JSONObject response) {
                    try {
                        // Log the status of the response
                        Log.d(Tag, "Response is: " + response.getString("status"));
                        
                        // Get the moves from the response
                        JSONArray moves = response.getJSONArray("moves");
                        JSONObject move;
                        
                        // Clear the previous moves list
                        movesList.clear();
                        
                        // Loop through the moves and create Move objects
                        for(int i = 0; i< moves.length();i++)
                        {
                            move = moves.getJSONObject(i);
                            movesList.add(new Move(move.getString("uci"),
                                    move.getString("san"),
                                    move.getString("note").substring(0,2),
                                    move.getString("winrate"),
                                    false));
                            
                            // Log each move
                            Log.d(Tag, "move is: " + move.getString("uci"));
                            
                            // If the maximum number of results has been reached, break the loop
                            if(i+1>=maxResults)
                            {
                                break;
                            }
                        }
                    } catch (JSONException e) {
                        // Print the stack trace if there is an error
                        e.printStackTrace();


                    }
                }
            }, new Response.ErrorListener() {
        @Override
        // Handle an error in the response
        public void onErrorResponse(VolleyError error) {
            // Log the error
            Log.d(Tag, "Not work");

        }
    });

    // Add the request to the RequestQueue.
    queue.add(jsonObjectRequest);
    
    // Return the list of moves
    return movesList;
}

    public List<Move> getMovesList() {
        return movesList;
    }

    //@Override
    //protected Void doInBackground(String... voids) {
    //    return null;
   // }
//    @Override
//    protected void onProgressUpdate(Void... progress) {
//        return null;
//    }
//    @Override
//    protected void onPostExecute(Void result) {
//        return null;
//    }
}
