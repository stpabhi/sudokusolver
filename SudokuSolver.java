import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SudokuSolver.java
 * 
 * @author Abhilash Srimat Tirumala Pallerlamudi
 * 
 * A program that uses recursive backtracking to solve a Sudoku puzzle.
 */
public class SudokuSolver {

    private static final int NUMBER_OF_ROWS_IN_SUDOKU_GRID = 9;
    private static final int NUMBER_OF_COLUMNS_IN_SUDOKU_GRID = 9;
    private static final int ROW_START_INDEX = 0;
    private static final int COLUMN_START_INDEX = 0;
    private static final int MIN_CELL_VALUE_IN_SUDOKU = 1;
    private static final int MAX_CELL_VALUE_IN_SUDOKU = 9;
    private static final int UNASSIGNED = 0;
    private int[][] sudokuGrid;
    private Point cellLocation;
    private List<Point> currentPath;

    public SudokuSolver(int[][] sudokuGrid, Point location) {
        this.sudokuGrid = sudokuGrid;
        this.cellLocation = location;
        currentPath = new ArrayList<>();
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("java <input file> <output file>");
            System.out.println("e.g. java input.csv output.csv");
        } else {
            String inputFileName = args[0];
            String outputFileName = args[1];
            int[][] sudokuGrid = readSudokuPuzzleFromCSVFile(inputFileName);
            Point startingLocation = new Point(ROW_START_INDEX, COLUMN_START_INDEX);

            if (sudokuGrid != null) {
                SudokuSolver sudokuSolver = new SudokuSolver(sudokuGrid, startingLocation);

                if (sudokuSolver.solveSudoku()) {
                    //clears the current list of decision points.
                    sudokuSolver.currentPath.clear();
                    System.out.println("Sudoku solved");
                    sudokuSolver.saveSudokuPuzzleSolutionToCSVFile(outputFileName);
                } else {
                    sudokuSolver.currentPath.clear();
                    System.out.println("Sudoku not solved");
                }
            }
        }
    }

    /**
     * solveSudoku()
     *
     * Solves the partially filled-in grid by assigning values to the unassigned
     * cells so that it meets the rules of Sudoku. This method uses recursive
     * backtracking. For every unassigned cell within the grid, it tries to
     * assign a value from 1 to 9 by checking whether it is safe to assign the
     * value. It returns true if the puzzle is solved. Returns false, if the
     * decision is wrong and sets the location to unassigned and backtracks to
     * the previous decision point.
     */
    private boolean solveSudoku() {
        if (isSudokuGridFull()) {
            return true;
        }
        for (int cellValue = MIN_CELL_VALUE_IN_SUDOKU; cellValue <= MAX_CELL_VALUE_IN_SUDOKU; cellValue++) {
            if (isSafe(cellLocation.x, cellLocation.y, cellValue)) {
                sudokuGrid[cellLocation.x][cellLocation.y] = cellValue;
                if (solveSudoku()) {
                    return true;
                }
                sudokuGrid[cellLocation.x][cellLocation.y] = UNASSIGNED;
                backtrackToPreviousDecisionPoint();
            }
        }
        return false;
    }

    /**
     * isSudokuGridFull()
     *
     * Helper method for solveSudoku() -- Searches the Sudoku grid to find any
     * unassigned cells. If found, the location is set to the current cell
     * location and adds the location to the current list of decision points and
     * true is returned. Returns false if there are no unassigned cells.
     */
    private boolean isSudokuGridFull() {
        for (int rowIndex = 0; rowIndex < sudokuGrid.length; rowIndex++) {
            for (int columnIndex = 0; columnIndex < sudokuGrid[rowIndex].length; columnIndex++) {
                if (sudokuGrid[rowIndex][columnIndex] == UNASSIGNED) {
                    cellLocation.x = rowIndex;
                    cellLocation.y = columnIndex;
                    saveToTraversedPath(new Point(cellLocation));
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * saveToTraversedPath()
     *
     * Helper method for isSudokuGridFull() -- Saves the current cell location
     * to the existing list of decision points.
     */
    private void saveToTraversedPath(Point currentLocation) {
        currentPath.add(currentLocation);
    }

    /**
     * backtrackToPreviousDecisionPoint()
     *
     * Helper method for solveSudoku() -- Backtracks to previous decision point.
     */
    private void backtrackToPreviousDecisionPoint() {
        currentPath.remove(getLastDecisionPoint());
        cellLocation.x = getLastDecisionPoint().x;
        cellLocation.y = getLastDecisionPoint().y;
    }

    /**
     * getLastDecisionPoint()
     *
     * Helper method for backtrackToPreviousDecisionPoint() -- Returns the last
     * entry of the decision points list.
     */
    private Point getLastDecisionPoint() {
        return currentPath.get(currentPath.size() - 1);
    }

    /**
     * isSafe()
     *
     * Helper method for solveSudoku() -- Returns a boolean whether it will be
     * legal to assign a number to the specified row, col location. The
     * assignment is legal if the given number is not already used in row, col,
     * or box.
     */
    private boolean isSafe(int rowIndex, int columnIndex, int cellValue) {
        return !isUsedInRow(rowIndex, cellValue) && !isUsedInCol(columnIndex, cellValue)
                && !isUsedInBox(rowIndex - rowIndex % 3, columnIndex - columnIndex % 3, cellValue);
    }

    /**
     * isUsedInRow()
     *
     * Helper method for isSafe() -- Returns a boolean whether assigned value
     * matches any number in the specified row.
     */
    private boolean isUsedInRow(int rowIndex, int cellValue) {
        for (int columnIndex = 0; columnIndex < sudokuGrid[rowIndex].length; columnIndex++) {
            if (sudokuGrid[rowIndex][columnIndex] == cellValue) {
                return true;
            }
        }
        return false;
    }

    /**
     * isUsedInCol()
     *
     * Helper method for isSafe() -- Returns a boolean whether assigned value
     * matches any number in the specified column.
     */
    private boolean isUsedInCol(int columnIndex, int cellValue) {
        for (int rowIndex = 0; rowIndex < sudokuGrid.length; rowIndex++) {
            if (sudokuGrid[rowIndex][columnIndex] == cellValue) {
                return true;
            }
        }
        return false;
    }

    /**
     * isUsedInBox()
     *
     * Helper method for isSafe() -- Returns a boolean whether assigned value
     * exists within the specified 3x3 box.
     */
    private boolean isUsedInBox(int boxStartRow, int boxStartCol, int cellValue) {
        for (int rowIndex = 0; rowIndex < 3; rowIndex++) {

            for (int columnIndex = 0; columnIndex < 3; columnIndex++) {
                if (sudokuGrid[rowIndex + boxStartRow][columnIndex + boxStartCol] == cellValue) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * readSudokuPuzzleFromCSVFile()
     *
     * Reads a Sudoku puzzle from input file and creates a grid.
     */
    private static int[][] readSudokuPuzzleFromCSVFile(String fileName) {
        int[][] sudokuGrid = new int[NUMBER_OF_ROWS_IN_SUDOKU_GRID][NUMBER_OF_COLUMNS_IN_SUDOKU_GRID];;
        BufferedReader bufferedReader = null;
        String line = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(fileName));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SudokuSolver.class.getName()).log(Level.SEVERE, "Unable to open the file requested", ex);
            System.exit(0);
        }
        try {
            for (int rowIndex = 0; (line = bufferedReader.readLine()) != null; rowIndex++) {
                StringTokenizer stringTokenizer = new StringTokenizer(line, ",");
                for (int columnIndex = 0; stringTokenizer.hasMoreElements(); columnIndex++) {
                    int cellValue = Integer.parseInt(stringTokenizer.nextElement().toString());
                    if (cellValue >= 0 && cellValue <= 9) {
                        sudokuGrid[rowIndex][columnIndex] = cellValue;
                    } else {
                        try {
                            throw new NumberFormatException();
                        } catch (NumberFormatException ex) {
                            Logger.getLogger(SudokuSolver.class.getName()).log(Level.SEVERE, "The cell values must be a number between 0 and 9", ex);
                            System.exit(0);
                        }
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(SudokuSolver.class.getName()).log(Level.SEVERE, "Unable to read from the input file", ex);
            System.exit(0);
        }
        return sudokuGrid;
    }

    /**
     * saveSudokuPuzzleSolutionToCSVFile()
     *
     * Saves Sudoku grid to output file
     */
    private void saveSudokuPuzzleSolutionToCSVFile(String fileName) {
        File file = new File(fileName);
        StringBuilder stringBuilder = new StringBuilder();
        try (FileWriter fileWriter = new FileWriter(file)) {
            for (int rowIndex = 0; rowIndex < sudokuGrid.length; rowIndex++) {
                for (int columnIndex = 0; columnIndex < sudokuGrid[rowIndex].length; columnIndex++) {
                    stringBuilder.append(sudokuGrid[rowIndex][columnIndex]);
                    stringBuilder.append(",");
                }
                stringBuilder = stringBuilder.deleteCharAt(stringBuilder.length() - 1); //removes comma at the end of string.
                stringBuilder.append("\n");
            }
            fileWriter.write(stringBuilder.toString());
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException ex) {
            Logger.getLogger(SudokuSolver.class.getName()).log(Level.SEVERE, "Unable to save the output file", ex);
            System.exit(0);
        }
    }
}
