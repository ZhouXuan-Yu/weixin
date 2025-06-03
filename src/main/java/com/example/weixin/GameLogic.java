package com.example.weixin;

public class GameLogic {
    private static final int BOARD_SIZE = 15;
    private static final int EMPTY = 0;
    private static final int PLAYER_X = 1;
    private static final int PLAYER_O = 2;

    private int[][] board;
    private int currentPlayer;

    public GameLogic() {
        board = new int[BOARD_SIZE][BOARD_SIZE];
        currentPlayer = PLAYER_X;
        initializeBoard();
    }

    private void initializeBoard() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board[i][j] = EMPTY;
            }
        }
    }

    public void makeMove(int row, int col) {
        if (isValidMove(row, col)) {
            board[row][col] = currentPlayer;
            currentPlayer = (currentPlayer == PLAYER_X) ? PLAYER_O : PLAYER_X;
        }
    }

    public boolean isValidMove(int row, int col) {
        return board[row][col] == EMPTY;
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public int getBoardSize() {
        return BOARD_SIZE;
    }

    public int getCellValue(int row, int col) {
        return board[row][col];
    }

    public boolean isGameOver() {
        return isWinningMove(PLAYER_X) || isWinningMove(PLAYER_O) || isBoardFull();
    }

    private boolean isWinningMove(int player) {
        // Check rows
        for (int i = 0; i < BOARD_SIZE; i++) {
            if (checkRow(i, player)) {
                return true;
            }
        }

        // Check columns
        for (int i = 0; i < BOARD_SIZE; i++) {
            if (checkColumn(i, player)) {
                return true;
            }
        }

        // Check diagonals
        if (checkDiagonal(player) || checkAntiDiagonal(player)) {
            return true;
        }

        return false;
    }

    private boolean checkRow(int row, int player) {
        for (int i = 0; i < BOARD_SIZE - 4; i++) {
            boolean win = true;
            for (int j = 0; j < 5; j++) {
                if (board[row][i + j] != player) {
                    win = false;
                    break;
                }
            }
            if (win) {
                return true;
            }
        }
        return false;
    }

    private boolean checkColumn(int col, int player) {
        for (int i = 0; i < BOARD_SIZE - 4; i++) {
            boolean win = true;
            for (int j = 0; j < 5; j++) {
                if (board[i + j][col] != player) {
                    win = false;
                    break;
                }
            }
            if (win) {
                return true;
            }
        }
        return false;
    }

    private boolean checkDiagonal(int player) {
        for (int i = 0; i < BOARD_SIZE - 4; i++) {
            for (int j = 0; j < BOARD_SIZE - 4; j++) {
                boolean win = true;
                for (int k = 0; k < 5; k++) {
                    if (board[i + k][j + k] != player) {
                        win = false;
                        break;
                    }
                }
                if (win) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkAntiDiagonal(int player) {
        for (int i = 4; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE - 4; j++) {
                boolean win = true;
                for (int k = 0; k < 5; k++) {
                    if (board[i - k][j + k] != player) {
                        win = false;
                        break;
                    }
                }
                if (win) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isBoardFull() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] == EMPTY) {
                    return false;
                }
            }
        }
        return true;
    }
}
