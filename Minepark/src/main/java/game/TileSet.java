package game;

public class TileSet {

    public static int[][] setTiles(int numRows, int numCols, int mines, Tile tile) {

        int[][] tiles = new int[numRows][numCols];
        tiles[tile.getRow()][tile.getCol()] = 0;
        int num = numCols * numRows;
        //System.out.println("Clicked tile is: " + tile.row + "," + tile.col);

        // MINES POSITIONING
        // Create an array of random positions for mines, no repetitions allowed
        int[] positions = new int[num];
        for (int i = 0; i < num; i++) {
            positions[i] = i;
        }
        for (int i = 0; i < mines; i++) {
            boolean isValid = false;
            int lastIndex = positions.length - 1 - i;
            int position = (int) (Math.random() * lastIndex);
            int mine = positions[position];
            int row = (int) Math.floor((1.0 * mine) / (numCols));
            int col = mine % (numCols);

            //System.out.println("Trying mine at: " + row + "," + col);
            //System.out.print("Mine " + mine + " taken from position " + position + " in range 0 to " + index);
            //System.out.println(" placed at (" + row + "," + col + ")");
            // Avoid placing mines in close range of first tile clicked tile
            // If position is not valid, try again 
            if (Math.abs(row - tile.getRow()) > 1 || Math.abs(col - tile.getCol()) > 1) {
                tiles[row][col] = 9;
                isValid = true;
            }
            if (!isValid) {
                i--;
                continue;
            }

            // Surrounding tiles numbering
            if (row < numRows - 1) {
                if (col > 0 && tiles[row + 1][col - 1] != 9) {
                    tiles[row + 1][col - 1] += 1;
                }
                if (tiles[row + 1][col] != 9) {
                    tiles[row + 1][col] += 1;
                }
                if (col < numCols - 1 && tiles[row + 1][col + 1] != 9) {
                    tiles[row + 1][col + 1] += 1;
                }
            }

            if (col < numCols - 1 && tiles[row][col + 1] != 9) {
                tiles[row][col + 1] += 1;
            }
            if (col > 0 && tiles[row][col - 1] != 9) {
                tiles[row][col - 1] += 1;
            }
            if (row > 0) {
                if (col > 0 && tiles[row - 1][col - 1] != 9) {
                    tiles[row - 1][col - 1] += 1;
                }
                if (tiles[row - 1][col] != 9) {
                    tiles[row - 1][col] += 1;
                }
                if (col < numCols - 1 && tiles[row - 1][col + 1] != 9) {
                    tiles[row - 1][col + 1] += 1;
                }
            }

            positions[position] = positions[lastIndex];

            // Check for random positioning, correct number of mines, no repetitions allowed
            //
            //			System.out.println("Mine " + positions[index] + " moved from position " + index + " to position " + position);
            //
            //			int count = 0;
            //			for (int[] row1: tiles) {
            //				for (int col1: row1) {
            //					System.out.print(col1);
            //					count += (col1 == 9 ? 1 : 0);
            //				}
            //				System.out.println();
            //			}
            //			System.out.println(count + " mines placed.");
        }
        return tiles;
    }
}
