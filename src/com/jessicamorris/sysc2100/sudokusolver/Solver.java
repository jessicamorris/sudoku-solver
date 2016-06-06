package com.jessicamorris.sysc2100.sudokusolver;

import java.io.*;
import java.util.*;

class Sudoku {
	private int[][] grid; // for storing current work/answer
	private int[][] firstSolution; // for storing a solution, if any
	private Sudoku preset; // reference for preset values
	private int numSolutions;
	
	/**
	 * Constructor for constructing the base puzzle.
	 * @param inputFilePath A String, path to the text file containing information on a Sudoku puzzle.
	 * @throws A generic Exception with a good error message, because I'm lazy sometimes.
	 */
	public Sudoku(String inputFilePath) throws Exception {
		grid = new int[9][9];
		firstSolution = null;
		preset = null;
		numSolutions = 0;
		
		// Initialize the grid and preset
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				grid[i][j] = 0;
			}
		}
		
		// Fill grid and preset with the input file's values
		try {
			Scanner sc = new Scanner(new File(inputFilePath));
			
			if (!sc.hasNextInt()) {
				sc.close();
				throw new Exception("File not formatted properly, couldn't find any numbers.");
			} else {
				int numEntries = sc.nextInt();
				
				for (int i = 0; i < numEntries; i++) {
					try {
						int row = sc.nextInt();
						int col = sc.nextInt();
						int value = sc.nextInt();
						
						if ((row > 9) || (row < 1) || (col > 9) || (col < 1) || (value > 9) || (value < 1)) {
							throw new Exception("File not formatted properly, a value was not between 1-9.");
						}
						
						grid[row-1][col-1] = value;
					} catch (Exception e) {
						sc.close();
						throw new Exception("Input file exhausted before all Sudoku details could be read. Check the format and try again.");
					}
				}
			}
			
			sc.close();
		}
		catch (FileNotFoundException e) {
			throw e;
		}
	}
	
	/**
	 * Constructor for constructing the copy of the puzzle that will be solved.
	 * @param preset A Sudoku object representing the puzzle's preset values.
	 */
	public Sudoku(Sudoku preset) {
		firstSolution = null;
		this.preset = preset;
		numSolutions = 0;
		
		grid = new int[preset.grid.length][];
		for (int i = 0; i < preset.grid.length; i++) {
			grid[i] = new int[preset.grid[i].length];
			System.arraycopy(preset.grid[i], 0, grid[i], 0, preset.grid[i].length);
		}
	}

	
	public String toString() {
		int[][] gridToPrint = (firstSolution == null) ? grid : firstSolution;
		String result = "";
		
		for (int i = 0; i < 9; i++) {
			if (i%3 == 0) {
				result += "+-------+-------+-------+\n";
			}
			for (int j = 0; j < 9; j++) {
				if (j%3 == 0) result += "| ";
				
				result += (gridToPrint[i][j] + " ");
				
				if (j == 8) result += "|";
			}
			result += ("\n");
		}
		result += "+-------+-------+-------+";
		
		return result;
	}
	
	
	public int getNumSolutions() {
		return numSolutions;
	}
	
	/**
	 * Helper method for solve, checks if the value is legal at the position indicated by (row, col)
	 * 
	 * @param row
	 *            Row component of the position being checked.
	 * @param col
	 *            Column component of the position being checked.
	 * @param value
	 *            The value being checked (1-9).
	 * @return true or false, is the value legal.
	 */
	private boolean isLegal(int row, int col, int value) {
		if (preset.grid[row][col] != 0) return false;
		// move is illegal if value is preset
		
		for (int i = 0; i < 9; i++) {
			if (grid[row][i] == value) return false;
			else if (grid[i][col] == value) return false;
		}
		// move is illegal if value already exists in the row/col

		int boxRow = (row / 3) * 3;
		int boxCol = (col / 3) * 3;
		
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				if (grid[boxRow + i][boxCol + j] == value) return false;
			}
		}
		// move is illegal if value already exists in box
		
		return true;
	}
	
	/**
	 *  Makes the first call to solve(row,col).
	 *  @throws A generic Exception, if you try to call this on a base-puzzle Sudoku instead of a solution Sudoku.
	 */
	public void solve() throws Exception {
		if (preset == null) {
			throw new Exception("Invalid Sudoku. This Sudoku has no preset; try setting up another Sudoku with this object as input.");
		}
		
		solve(0,0);
	}
	
	/**
	 * Solves the Sudoku via brute-force.
	 * @param row The row component being checked.
	 * @param col The column component being checked.
	 */
	private void solve(int row, int col) {
		if (row > 8) {
			// Only way this can be reached is if all columns and rows have been filled successfully.
			// If this is the first solution we run into, make a copy and save it for later.
			if (numSolutions++ == 0) {
				firstSolution = new int[grid.length][];
				for (int i = 0; i < grid.length; i++) {
					firstSolution[i] = new int[grid[i].length];
					System.arraycopy(grid[i], 0, firstSolution[i], 0, grid[i].length);
				}
			}
			
			// Reset on backtrack
			grid[row-1][col] = preset.grid[row-1][col];
			return;
		}
		
		// Check if the position is cleared
		if (grid[row][col] != 0) {
			nextSolve(row, col);
		}
		
		// Try numbers 1-9 at this position
		for (int num = 1; num < 10; num++) {
			if (isLegal(row, col, num)) {
				grid[row][col] = num;
				nextSolve(row, col);
			}
		}
		
		// Reset on backtrack
		grid[row][col] = preset.grid[row][col];
	}
	
	private void nextSolve(int row, int col) {
		if (col < 8) {
			// Iterate along the row
			solve(row, col+1);
		}
		else {
			// Reached the end of the row, start the next one
			solve(row+1, 0);
		}
	}

}

public class Solver {

	public static void main(String[] args) {
		String inputFilepath;
		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		
		System.out.print("Enter a file name containing a Sudoku puzzle: ");
		
		try {
			inputFilepath = input.readLine(); // get filename
		} catch (IOException e) {
			System.err.println("ERROR: " + e);
			return;
		}
		
		// Generate the starting puzzle from the file
		Sudoku puzzle;
		try {
			puzzle = new Sudoku(inputFilepath);
		} catch (Exception e) {
			System.err.println("ERROR: " + e);
			return;
		}
		
		// Generate the puzzle solution (if any)
		Sudoku solution = new Sudoku(puzzle);
		try {
			solution.solve();
		} catch (Exception e) {
			System.out.println("Well that shouldn't have happened...");
		}
		
		System.out.println("     STARTING MATRIX     ");
		System.out.println(puzzle.toString());

		if (solution.getNumSolutions() > 1) {
			System.out.println("Found "+ solution.getNumSolutions() +" solutions.");
		}
		
		if (solution.getNumSolutions() > 0) {
			System.out.println("\n    SOLUTION MATRIX    ");
			System.out.println(solution.toString());
		}
		else {
			System.out.println("Sudoku has no solutions.");
		}
	}

}
