package com.example.next_move_chess_scanner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PieceListAdapter extends RecyclerView.Adapter<PieceListAdapter.PieceViewHolder>  {
    private List<Piece> pieceList;
    private final LayoutInflater inflater;
    private PieceListAdapter.AdapterCallback mAdapterCallback;

    public PieceListAdapter(Context context, List<Piece> pieceList) {
        inflater = LayoutInflater.from(context);
        this.pieceList = pieceList;
        try {
            this.mAdapterCallback = ((PieceListAdapter.AdapterCallback) context);
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement AdapterCallback.");
        }
    }

    public void setPieceList(List<Piece> pieceList) {
        this.pieceList = pieceList;
    }

    class PieceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public final TextView pieceField;
        public final ImageView pieceFieldPhoto;
        public final TextView pieceRecognized;
        public final TextView pieceConfidence;
        final PieceListAdapter adapter;

        public PieceViewHolder(@NonNull View itemView, PieceListAdapter adapter) {
            super(itemView);

            pieceField = itemView.findViewById(R.id.field);
            pieceFieldPhoto = itemView.findViewById(R.id.fieldPhoto);
            pieceRecognized = itemView.findViewById(R.id.recognizedPiece);
            pieceConfidence =  itemView.findViewById(R.id.confidence);
            this.adapter = adapter;
            itemView.setOnClickListener(this);
        }
        @Override
        public void onClick(View view) {

        }

    }

    @NonNull
    @Override
    public PieceListAdapter.PieceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.piece_list_item, parent, false);
        return new PieceListAdapter.PieceViewHolder(itemView, this);
    }

    @Override
    public void onBindViewHolder(@NonNull PieceListAdapter.PieceViewHolder holder, int position) {
        Piece current = pieceList.get(position);


        holder.pieceField.setText(current.getField());
        holder.pieceFieldPhoto.setImageBitmap(current.getFieldPhoto());
        holder.pieceRecognized.setText(current.getRecognized());
        holder.pieceConfidence.setText(current.getConfidence()*100+" %");
    }

    @Override
    public int getItemCount() {
        return pieceList.size();
    }
    public static interface AdapterCallback {
        void onMethodCallback(String yourValue);
    }
}
