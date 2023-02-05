package com.example.next_move_chess_scanner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MoveListAdapter extends RecyclerView.Adapter<MoveListAdapter.MoveViewHolder> {

    private List<Move> moveList;
    private final LayoutInflater inflater;
    private final AdapterCallback mAdapterCallback;
    private final int defaultColor;
    private final int selectedColor;
    int selectedPosition=0;
    private boolean sanNotation = false;



    public MoveListAdapter(Context context, List<Move> moveList, boolean sanNotation) {
        inflater = LayoutInflater.from(context);
        this.moveList = moveList;
        this.sanNotation = sanNotation;
        try {
            this.mAdapterCallback = ((AdapterCallback) context);
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement AdapterCallback.");
        }
        defaultColor = ContextCompat.getColor(context, R.color.white);
        selectedColor = ContextCompat.getColor(context, R.color.marked);
    }

    public void setMoveList(List<Move> moveList) {
        this.moveList = moveList;
    }

    class MoveViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public final TextView move;
        public final TextView moveWinRate;
        public final TextView moveNote;
        final MoveListAdapter adapter;

        public MoveViewHolder(@NonNull View itemView, MoveListAdapter adapter) {
            super(itemView);
            move = itemView.findViewById(R.id.move);
            moveWinRate = itemView.findViewById(R.id.winrate);
            moveNote = itemView.findViewById(R.id.note);
            this.adapter = adapter;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int previousPosition = selectedPosition;
            selectedPosition = getAdapterPosition();
            mAdapterCallback.onMethodCallback(moveList.get(selectedPosition).getUCIMove());
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);
        }

    }

    @NonNull
    @Override
    public MoveListAdapter.MoveViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.move_list_item, parent, false);
        return new MoveViewHolder(itemView, this);
    }

    @Override
    public void onBindViewHolder(@NonNull MoveViewHolder holder, int position) {
        Move current = moveList.get(position);
        holder.move.setText(sanNotation ? current.getSANMove() : current.getUCIMove());
        holder.moveWinRate.setText(String.valueOf(current.getWinRate()));
        holder.moveNote.setText(current.getNote());
        holder.itemView.setBackgroundColor(selectedPosition == position ? selectedColor : defaultColor);
    }

    @Override
    public int getItemCount() {
        return moveList.size();
    }
    public static interface AdapterCallback {
        void onMethodCallback(String yourValue);
    }
}