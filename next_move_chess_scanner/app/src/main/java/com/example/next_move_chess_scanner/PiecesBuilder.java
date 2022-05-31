package com.example.next_move_chess_scanner;

import java.util.HashMap;
import java.util.Map;

public class PiecesBuilder {
    Map<String, Integer> pieces = new HashMap<String, Integer>();

    public PiecesBuilder() {
        pieces.put("black_bishop", R.drawable.ic_bb);
        pieces.put("black_king", R.drawable.ic_bk);
        pieces.put("black_knight", R.drawable.ic_bn);
        pieces.put("black_pawn", R.drawable.ic_bp);
        pieces.put("black_queen", R.drawable.ic_bq);
        pieces.put("black_rook", R.drawable.ic_br);
        pieces.put("white_bishop", R.drawable.ic_wb);
        pieces.put("white_king", R.drawable.ic_wk);
        pieces.put("white_knight", R.drawable.ic_wn);
        pieces.put("white_pawn", R.drawable.ic_wp);
        pieces.put("white_queen", R.drawable.ic_wq);
        pieces.put("white_rook", R.drawable.ic_wr);
    }
}
