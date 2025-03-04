package chess.pieces;

import boardGame.Board;
import boardGame.Position;
import chess.ChessMatch;
import chess.ChessPiece;
import chess.Color;

public class Pawn extends ChessPiece {

	private ChessMatch chessMatch;	
	
	public Pawn(Board board, Color color, ChessMatch chessmatch) {
		super(board, color);
		this.chessMatch = chessmatch;
	}

	@Override
	public boolean[][] possibleMoves() {
		boolean[][] mat = new boolean[getBoard().getRows()][getBoard().getColumns()];

		Position p = new Position(0, 0);
		
		if(getColor() == Color.White) {
			p.setValues(position.getRow() - 1, position.getColumn());
			if(getBoard().positionExists(p) && !getBoard().thereIsAPiece(p)) {
				mat[p.getRow()][p.getColumn()] = true;
			}
		
		
			p.setValues(position.getRow() - 2, position.getColumn());
			Position p2 = new Position(position.getRow() - 1, position.getColumn());
			if(getBoard().positionExists(p) && !getBoard().thereIsAPiece(p) && getBoard().positionExists(p2) && !getBoard().thereIsAPiece(p2) && getMoveCount() == 0) {
				mat[p.getRow()][p.getColumn()] = true;
			}
			
			p.setValues(position.getRow() - 1, position.getColumn() - 1);
			if(getBoard().positionExists(p) && isThereOpponnentPiece(p)) {
				mat[p.getRow()][p.getColumn()] = true;
			}
			
			p.setValues(position.getRow() - 1, position.getColumn() + 1);
			if(getBoard().positionExists(p) && isThereOpponnentPiece(p)) {
				mat[p.getRow()][p.getColumn()] = true;
			}
			
			//special move en passant
			if(position.getRow() == 3) {
				Position left = new Position(position.getRow(),position.getColumn() - 1);
				if(getBoard().positionExists(left) && isThereOpponnentPiece(left) && getBoard().piece(left) == chessMatch.getenPassantVulnerable()) {
					mat[left.getRow() - 1][left.getColumn()] = true;
				};

				Position right = new Position(position.getRow(),position.getColumn() + 1);
				if(getBoard().positionExists(right) && isThereOpponnentPiece(left) && getBoard().piece(left) == chessMatch.getenPassantVulnerable()) {
					mat[left.getRow() - 1][left.getColumn()] = true;
				};
			}
			
		}else {
			
			p.setValues(position.getRow() + 1, position.getColumn());
			if(getBoard().positionExists(p) && !getBoard().thereIsAPiece(p)) {
				mat[p.getRow()][p.getColumn()] = true;
			}
		
		
			p.setValues(position.getRow() + 2, position.getColumn());
			Position p2 = new Position(position.getRow() + 1, position.getColumn());
			if(getBoard().positionExists(p) && !getBoard().thereIsAPiece(p) && getBoard().positionExists(p2) && !getBoard().thereIsAPiece(p2) && getMoveCount() == 0) {
				mat[p.getRow()][p.getColumn()] = true;
			}
			
			p.setValues(position.getRow() + 1, position.getColumn() - 1);
			if(getBoard().positionExists(p) && isThereOpponnentPiece(p)) {
				mat[p.getRow()][p.getColumn()] = true;
			}
			
			p.setValues(position.getRow() + 1, position.getColumn() + 1);
			if(getBoard().positionExists(p) && isThereOpponnentPiece(p)) {
				mat[p.getRow()][p.getColumn()] = true;
			}
			
			if(position.getRow() == 4) {
				Position left = new Position(position.getRow(),position.getColumn() - 1);
				if(getBoard().positionExists(left) && isThereOpponnentPiece(left) && getBoard().piece(left) == chessMatch.getenPassantVulnerable()) {
					mat[left.getRow() + 1][left.getColumn()] = true;
				};

				Position right = new Position(position.getRow(),position.getColumn() + 1);
				if(getBoard().positionExists(right) && isThereOpponnentPiece(left) && getBoard().piece(left) == chessMatch.getenPassantVulnerable()) {
					mat[left.getRow() + 1][left.getColumn()] = true;
				};
			}
			
		}
			
		return mat;
	}

	@Override
	public String toString() {
		return String.format("P");
	}
	
	
	
	

}
