package chess;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import boardGame.Board;
import boardGame.Piece;
import boardGame.Position;
import chess.pieces.Bishop;
import chess.pieces.King;
import chess.pieces.Knight;
import chess.pieces.Pawn;
import chess.pieces.Queen;
import chess.pieces.Rook;

public class ChessMatch {
	private int turn;
	private Color currentPlayer;
	private Board board;
	private boolean check;
	private boolean checkMate;
	private ChessPiece enpassantVulnerable;
	private ChessPiece promoted;
	
	private List<Piece> piecesOnTheBoard = new ArrayList();
	private List<Piece> capturedPieces = new ArrayList();
	
	public ChessMatch() {
		board = new Board(8,8);
		turn = 1;
		currentPlayer = Color.White;
		initialSetup();
	}
	
	public int getTurn() {
		return turn;
	}
	public Color getCurrentPlayer() {
		return currentPlayer;
	}
	
	public boolean getCheck() {
		return check;
	}
	
	public boolean getCheckMate() {
		return checkMate;
	}
	public ChessPiece getenPassantVulnerable() {
		return enpassantVulnerable; 
	}
		
	public ChessPiece getPromoted() {
		return promoted;
	}

	public ChessPiece[][] getPieces() {
		ChessPiece[][] mat = new ChessPiece[board.getRows()][board.getColumns()];
		for(int i = 0; i<board.getRows();i++) {
			for(int j = 0;j<board.getColumns();j++) {
				mat[i][j] = (ChessPiece) board.piece(i,j);
				
			}
		}
		return mat;
	}
	
	public boolean[][] possibleMoves(ChessPosition sourcePosition) {
		Position position = sourcePosition.toPosition();
		validateSourcePosition(position);
		return board.piece(position).possibleMoves();
	}
	
	public ChessPiece performChessMove(ChessPosition sourcePosition, ChessPosition targetChessPosition) {
		Position source = sourcePosition.toPosition();
		Position target = targetChessPosition.toPosition();
		validateSourcePosition(source);
		validateTargetPosition(source , target);
		Piece CapturedPiece = makeMove(source,target);
		
		if(testCheck(currentPlayer)) {
			UndoMove(source, target, CapturedPiece);
			
			throw new ChessException("You can´t put youself in check");
		}
		
		ChessPiece movePiece = (ChessPiece)board.piece(target);
		
		//special move promotion
		promoted = null;
		if(movePiece instanceof Pawn) { 
			if(movePiece.getColor() == Color.White && target.getRow() == 0 || movePiece.getColor() == Color.Black && target.getRow() == 7 ) {
				promoted = (ChessPiece)board.piece(target);
				promoted = replacePromotedPiece("Q");
			}
		}
		
		check = (testCheck(opponnent(currentPlayer))) ? true : false;
		
		
		
		if(testeCheckMate(opponnent(currentPlayer))) {
			checkMate = true;
		}else {
			nextTurn();
		}
		
		//Special move en Passant
		if(movePiece instanceof Pawn && (target.getRow() == source.getRow() - 2 || target.getRow() == source.getRow() + 2 ))
			enpassantVulnerable = movePiece;
		 	
		else {
			enpassantVulnerable = null;
		}
		
		return (ChessPiece)CapturedPiece;
	}
	
	
	
	public void validateSourcePosition(Position position) {
		if(!board.thereIsAPiece(position)) {
			throw new ChessException("No exists piece on souce position"); 
		}
		if(currentPlayer != ((ChessPiece)board.piece(position)).getColor()) {
			throw new ChessException("The chosen piece is not yours");
		}
		if(!board.piece(position).isTherePossibleMove()) {
			throw new ChessException("There is no possible moves for the chosen piece");
		}
		
		
	}
	
	private void validateTargetPosition(Position source, Position target) {
		if(!board.piece(source).possibleMove(target)) {
			throw new ChessException("The chosen piece can't move to target position");
		}
	}
	
	private void nextTurn() {
		turn++;
		currentPlayer = (currentPlayer == Color.White) ? Color.Black : Color.White;
	}
	
	public ChessPiece replacePromotedPiece(String type) {
		if(promoted == null) {
			throw new IllegalStateException("There is no piece to be promoted"); 
		}
		if(!type.equals("B") && !type.equals("N") && !type.equals("R") && !type.equals("Q") ) {
			return promoted;
		}
		
		Position pos = promoted.getChessPosition().toPosition();
		Piece p = board.removePiece(pos);
		piecesOnTheBoard.remove(p);
		
		ChessPiece newPiece = newPiece(type,promoted.getColor());
		board.placePiece(newPiece, pos);
		piecesOnTheBoard.add(newPiece);
		return newPiece;
		
		
	}
	
	private ChessPiece newPiece(String type, Color color) {
		if(type.equals("B")) return new Bishop(board,color);
		if(type.equals("N")) return new Knight(board,color);
		if(type.equals("Q")) return new Queen(board,color);
		return new Rook (board,color);
	}
	
	private Piece makeMove(Position source, Position target) {
		ChessPiece p = (ChessPiece)board.removePiece(source);
		p.increaseMoveCount();
		Piece capturedPiece = board.removePiece(target);
		board.placePiece(p, target);
		
		if(capturedPiece != null) {
			piecesOnTheBoard.remove(capturedPiece);
			capturedPieces.add(capturedPiece);
		}
		
		//Special move castling kingside move
		if(p instanceof King && target.getColumn() == source.getColumn() + 2  ) {
			Position SourceT = new Position(source.getRow(), source.getColumn() + 3);
			Position TargetT = new Position(source.getRow(), source.getColumn() + 1);
			ChessPiece rook = (ChessPiece)board.removePiece(SourceT);
			board.placePiece(rook, TargetT);
			rook.increaseMoveCount();
		}
		
		//Special castling queenside move
		if(p instanceof King && target.getColumn() == source.getColumn() - 2  ) {
			Position SourceT = new Position(source.getRow(), source.getColumn() - 4);
			Position TargetT = new Position(source.getRow(), source.getColumn() - 1);
			ChessPiece rook = (ChessPiece)board.removePiece(SourceT);
			board.placePiece(rook, TargetT);
			rook.increaseMoveCount();
		}
		
		//special move en passant
		
		if(p instanceof Pawn) {
			if(source.getColumn() != target.getColumn()&& capturedPiece == null ){
				Position pawnPosition;
				if(p.getColor() == Color.White) {
					pawnPosition = new Position(target.getRow() + 1,target.getColumn());
				}else {
					pawnPosition = new Position(target.getRow() - 1,target.getColumn());
				}
				capturedPiece = board.removePiece(pawnPosition);
				capturedPieces.add(capturedPiece);
				piecesOnTheBoard.remove(capturedPiece);
			}
		}
		
		return capturedPiece;
	}
	
	private void UndoMove(Position source, Position target, Piece capturedPiece) {
		ChessPiece p = (ChessPiece)board.removePiece(target);
		p.decreaseMoveCount();
		board.placePiece(p, source);
		
		if(capturedPiece != null) {
			board.placePiece(capturedPiece, target);
			capturedPieces.remove(capturedPiece);
			piecesOnTheBoard.add(capturedPiece);
		}
		
		//Special move castling kingside move
				if(p instanceof King && target.getColumn() == source.getColumn() + 2  ) {
					Position SourceT = new Position(source.getRow(), source.getColumn() + 3);
					Position TargetT = new Position(source.getRow(), source.getColumn() + 1);
					ChessPiece rook = (ChessPiece)board.removePiece(TargetT);
					board.placePiece(rook, SourceT);
					rook.decreaseMoveCount();
				}
				
				//Special castling queenside move
				if(p instanceof King && target.getColumn() == source.getColumn() - 2  ) {
					Position SourceT = new Position(source.getRow(), source.getColumn() - 4);
					Position TargetT = new Position(source.getRow(), source.getColumn() - 1);
					ChessPiece rook = (ChessPiece)board.removePiece(TargetT);
					board.placePiece(rook, SourceT);
					rook.decreaseMoveCount();
				}
				
				
				if(p instanceof Pawn) {
					if(source.getColumn() != target.getColumn()&& capturedPiece == enpassantVulnerable ){
						ChessPiece pawn = (ChessPiece) board.removePiece(target);
						
						Position pawnPosition;
						if(p.getColor() == Color.White) {
							pawnPosition = new Position(3,target.getColumn());
						}else {
							pawnPosition = new Position(4 - 1,target.getColumn());
						}
						board.placePiece(pawn, pawnPosition);
					}
				}
		
	}
	
	private Color opponnent(Color color) {
		return (color == Color.White) ? Color.Black : Color.White;
	}
	
	private ChessPiece king(Color color) {
		List<Piece> list = piecesOnTheBoard.stream().filter(x -> ((ChessPiece)x).getColor() == color).collect(Collectors.toList());
		for(Piece p : list) {
			if(p instanceof King) {
				return (ChessPiece) p;
			}
		}
		throw new IllegalStateException("There is no " + color + "  king on the board");
	}
	
	private boolean testCheck(Color color) {
		Position kingPosition = king(color).getChessPosition().toPosition();
		List<Piece> opponnentPieces = piecesOnTheBoard.stream().filter(x -> ((ChessPiece)x).getColor() == opponnent(color)).collect(Collectors.toList());
		for(Piece p : opponnentPieces) {
			boolean[][] mat = p.possibleMoves();
			if(mat[kingPosition.getRow()][kingPosition.getColumn()]) {
				return true;
			}
		}
		return false;
	}
	
	private boolean testeCheckMate(Color color) {
		if(!testCheck(color)) {
			return false;
		}
		List<Piece> list = piecesOnTheBoard.stream().filter(x -> ((ChessPiece)x).getColor() == color).collect(Collectors.toList());
		for(Piece p : list) {
			boolean[][] mat = p.possibleMoves();
			for(int i=0;i<board.getRows();i++) {
				for(int j=0;j<board.getColumns();j++) {
					if(mat[i][j]) {
						Position source = ((ChessPiece)p).getChessPosition().toPosition();
						Position target = new Position(i,j);
						Piece capturedPiece = makeMove(source, target);
						boolean testCheck = testeCheckMate(color);
						UndoMove(source, target, capturedPiece);
						if(!testCheck) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}
	
	
	private void placeNewPiece(char column, int row, ChessPiece piece) {
		board.placePiece(piece, new ChessPosition(column,row).toPosition());
		piecesOnTheBoard.add(piece);
	}
	
	private void initialSetup() {

		 placeNewPiece('a', 1, new Rook(board, Color.White));
	        placeNewPiece('b', 1, new Knight(board, Color.White));
	        placeNewPiece('c', 1, new Bishop(board, Color.White));
	        placeNewPiece('d', 1, new Queen(board, Color.White));
	        placeNewPiece('e', 1, new King(board, Color.White, this));
	        placeNewPiece('f', 1, new Bishop(board, Color.White));
	        placeNewPiece('g', 1, new Knight(board, Color.White));
	        placeNewPiece('h', 1, new Rook(board, Color.White));
	        placeNewPiece('a', 2, new Pawn(board, Color.White, this));
	        placeNewPiece('b', 2, new Pawn(board, Color.White, this));
	        placeNewPiece('c', 2, new Pawn(board, Color.White, this));
	        placeNewPiece('d', 2, new Pawn(board, Color.White, this));
	        placeNewPiece('e', 2, new Pawn(board, Color.White, this));
	        placeNewPiece('f', 2, new Pawn(board, Color.White, this));
	        placeNewPiece('g', 2, new Pawn(board, Color.White, this));
	        placeNewPiece('h', 2, new Pawn(board, Color.White, this));

	        placeNewPiece('a', 8, new Rook(board, Color.Black));
	        placeNewPiece('b', 8, new Knight(board, Color.Black));
	        placeNewPiece('c', 8, new Bishop(board, Color.Black));
	        placeNewPiece('d', 8, new Queen(board, Color.Black));
	        placeNewPiece('e', 8, new King(board, Color.Black, this));
	        placeNewPiece('f', 8, new Bishop(board, Color.Black));
	        placeNewPiece('g', 8, new Knight(board, Color.Black));
	        placeNewPiece('h', 8, new Rook(board, Color.Black));
	        placeNewPiece('a', 7, new Pawn(board, Color.Black,this));
	        placeNewPiece('b', 7, new Pawn(board, Color.Black,this));
	        placeNewPiece('c', 7, new Pawn(board, Color.Black,this));
	        placeNewPiece('d', 7, new Pawn(board, Color.Black,this));
	        placeNewPiece('e', 7, new Pawn(board, Color.Black,this));
	        placeNewPiece('f', 7, new Pawn(board, Color.Black,this));
	        placeNewPiece('g', 7, new Pawn(board, Color.Black,this));
	        placeNewPiece('h', 7, new Pawn(board, Color.Black,this));
	}
	

}
