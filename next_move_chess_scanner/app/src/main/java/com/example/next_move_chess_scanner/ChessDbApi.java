package com.example.next_move_chess_scanner;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.OpenCVLoader;

import java.util.ArrayList;
import java.util.List;


public class ChessDbApi{
    private static List<Move> movesList = new ArrayList<>();
    private static final String baseUrl = "https://www.chessdb.cn/cdb.php?action=queryall&board=";
    private static final String returnJson = "&json=1";
    private static Context ctx;
    private static final String Tag = "CHESSDBAPI";

    ChessDbApi(Context context) {
        ctx = context;
    }

    public List<Move> sendRequest(String fenNotation) {
        String completedURL = baseUrl + fenNotation + returnJson;
        RequestQueue queue = Volley.newRequestQueue(this.ctx);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, completedURL,null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d(Tag, "Response is: " + response.getString("status"));
                            JSONArray moves = response.getJSONArray("moves");
                            JSONObject move;
                            movesList.clear();
                            for(int i = 0; i< moves.length();i++)
                            {
                                move = moves.getJSONObject(i);
                                movesList.add(new Move(move.getString("uci"),
                                        move.getString("san"),
                                        move.getInt("score"),
                                        move.getInt("rank"),
                                        move.getString("note").substring(0,2),
                                        move.getString("winrate"),
                                        false));
                                if (i>=10){
                                    break;
                                }
                                Log.d(Tag, "move is: " + move.getString("score"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(Tag, "That didn't work!");
            }
        });

        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);
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
