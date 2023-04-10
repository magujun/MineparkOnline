package game;

import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;

public class Grid extends GridPane {

    private int numRows, numCols, mines, tileSize;
    private final double GRIDSIZE = 500;
    private int[][] tiles;
    private String gridState;

    public Grid(Game game) {

        this.numCols = game.getNumCols();
        this.numRows = game.getNumRows();
        this.mines = game.getMines();
        // Create a fixed grid with calculated tile size
        this.tileSize = (int) (GRIDSIZE / numRows);
        this.tiles = new int[numRows][numCols];
        setPrefSize(tileSize * numCols, tileSize * numRows);

        // Create an array of clicks for the counter inside the event handler
        int[] clicks = new int[1];

        // INITIAL GRID LAYOUT
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                Tile tile = new Tile(game, this, GRIDSIZE, tileSize, tiles);
                tile.setRow(row);
                tile.setCol(col);

                // Set new tile properties, according to predetermined click events for the tile
                // Handle tease click
                tile.setOnMousePressed(e -> {
                    MouseButton mouseButton = e.getButton();
                    if (mouseButton == MouseButton.PRIMARY) {
                        tile.tease();
                    }
                });

                // Handle proper mouse click
                tile.setOnMouseClicked(e -> {
                    MouseButton mouseButton = e.getButton();
                    // Handle first click on the grid
                    // First click is always on an empty tile
                    // Position the mines to set the grid layout and increment click count
                    if (clicks[0] == 0) {
                        if (mouseButton == MouseButton.PRIMARY) {
                            game.getMusic().stop();
                            game.setMusic(new Music("newGame", game.getDifficulty()));
                            game.getMusic().start();
                            tiles = TileSet.setTiles(numRows, numCols, mines, tile);
                            tileSet();
                            tile.click();
                            game.getTimeline().play();
                            getState(tile, tiles, numRows, numCols);
                            clicks[0]++;
                        }
                    } // Handle secondary mouse button click, which sets flags on and off
                    // The number of flagged mines has to be less than the total number of mines
                    // The tile state has to be "covered"
                    // Also Check for flagged mines around a cleared tile
                    // If all surrounding mines are correctly flagged, clear surrounding tiles
                    else if (mouseButton == MouseButton.SECONDARY) {
                        tile.flag();
                        tile.check();
                        getState(tile, tiles, numRows, numCols);
                    } // Handle primary single click on a tile, checks for state, flags and mines
                    else {
                        tile.click();
                        getState(tile, tiles, numRows, numCols);
                    }
                });

                // Add tiles to grid pane
                add(tile, col, row);
            }
        }
    }

    public String getState(Tile tile, int[][] tiles, int numRows, int numCols) {

        gridState = "";
        // Update grid state and output layout to console
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                int item = row * numCols + col;
                tile = (Tile) getChildren().get(item);
                char tileState = (tile.isFlagged() ? 'F' : tile.isMine() ? 'X' : tile.isChecked() ? 'C' : (char) (tiles[row][col] + '0'));
                System.out.print(tileState + " ");
                gridState += tileState;
            }
            System.out.println();
        }
        System.out.println();
        System.out.println(gridState);
        System.out.println();
        return gridState;
    }

    public void setState(int[][] tiles, int numRows, int numCols, String gridState) {

        // Update grid state 
        // Debug: output layout to console
        Tile tile;
        char tileState;
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                int item = row * numCols + col;
                tileState = gridState.charAt(item);
                tile = (Tile) getChildren().get(item);
                switch (tileState) {
                    case 'F':
                        tile.flag();
                        break;
                    case 'X':
                        tile.mine();
                        break;
                    case 'C':
                        tile.clear();
                        break;
                    default:
                        tile.setTileNumber(tiles[row][col] + '0');
                        break;
                }
                System.out.print(tileState + " ");
                gridState += tileState;
                tile.setType(tiles[row][col]);
            }
            System.out.println();
        }
        System.out.println();
        System.out.println(gridState);
    }

    // Getters and Setters
    public int[][] getTiles() {
        return tiles;
    }

    /**
     * @return the tileSize
     */
    public int getTileSize() {
        return tileSize;
    }

    /**
     * @param tileSize the tileSize to set
     */
    public void setTileSize(int tileSize) {
        this.tileSize = tileSize;
    }

    /**
     * @return the gridState
     */
    public String getGridState() {
        return gridState;
    }

    /**
     * @param gridState the gridState to set
     */
    public void setGridState(String gridState) {
        this.gridState = gridState;
    }

    /**
     * @param tiles the tiles to set
     */
    public void setTiles(int[][] tiles) {
        this.tiles = tiles;
    }

    public void tileSet() {
        // TILES POSITIONING
        // On first click, get mines positioning
        // Update grid state and output layout to console
        Tile tile;
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                int item = row * numCols + col;
                tile = (Tile) getChildren().get(item);
                tile.setType(tiles[row][col]);
            }
        }
    }

}
