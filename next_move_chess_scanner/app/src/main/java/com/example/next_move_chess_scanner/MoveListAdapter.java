package com.example.next_move_chess_scanner;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MoveListAdapter extends RecyclerView.Adapter<MoveListAdapter.MoveViewHolder> {

    private List<Move> moveList;
    private final LayoutInflater inflater;
    int selectedPosition=0;
    private AdapterCallback mAdapterCallback;

    public MoveListAdapter(Context context, List<Move> moveList) {
        inflater = LayoutInflater.from(context);
        this.moveList = moveList;
        try {
            this.mAdapterCallback = ((AdapterCallback) context);
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement AdapterCallback.");
        }
    }

    public void setMoveList(List<Move> moveList) {
        this.moveList = moveList;
    }

    class MoveViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public final TextView moveUCI;
        public final TextView moveScore;
        public final TextView moveWinrate;
        public final TextView moveNote;
        final MoveListAdapter adapter;

        public MoveViewHolder(@NonNull View itemView, MoveListAdapter adapter) {
            super(itemView);
            moveUCI = itemView.findViewById(R.id.UCImove);
            moveScore = itemView.findViewById(R.id.score);
            moveWinrate = itemView.findViewById(R.id.winrate);
            moveNote = itemView.findViewById(R.id.note);
            this.adapter = adapter;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            selectedPosition = getAdapterPosition();
            mAdapterCallback.onMethodCallback(moveList.get(selectedPosition).getUCImove());
            notifyDataSetChanged();
        }

    }

    @NonNull
    @Override
    public MoveListAdapter.MoveViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.move_list_item, parent, false);
        return new MoveViewHolder(itemView, this);
    }

    @Override
    public void onBindViewHolder(@NonNull MoveListAdapter.MoveViewHolder holder, int position) {
        Move current = moveList.get(position);
        holder.moveUCI.setText(current.getUCImove());
        holder.moveScore.setText(String.valueOf(current.getScore()));
        holder.moveWinrate.setText(String.valueOf(current.getWinrate()));
        holder.moveNote.setText(current.getNote());

        if (selectedPosition==position){
            holder.itemView.setBackgroundColor(Color.parseColor("#cbfac3"));
        }
        else{
            holder.itemView.setBackgroundColor(Color.parseColor("#ffffff"));
        }

    }

    @Override
    public int getItemCount() {
        return moveList.size();
    }
    public static interface AdapterCallback {
        void onMethodCallback(String yourValue);
    }
}