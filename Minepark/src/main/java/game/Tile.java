package game;

import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

public class Tile extends Button {

    private int type; // type is a number meaning (0 to 8) for points of contact with mines and (9) for mines
    private int row, col, rows, cols;
    private double tileSize;
    private int[][] tiles;
    private boolean covered, flagged, checked, mine; // !covered is cleared, flagged needs no explanation
    private ImageView imageTile, imageMine, imageFlag, imageMisflag, imageSure;
    private Game game;
    private GridPane grid;
    private SoundEffect effect;

    // Tile constructor
    public Tile(Game game, GridPane grid, double gridSize, int tileSize, int[][] tiles) {

        this.game = game;
        this.grid = grid;
        this.tileSize = tileSize;
        this.tiles = tiles;
        this.rows = tiles.length;
        this.cols = tiles[0].length;

        setMinWidth(tileSize);
        setMinHeight(tileSize);
        setMaxWidth(tileSize);
        setMaxHeight(tileSize);

        int tile = (int) (Math.random() * 4);
        imageTile = new ImageView(new Image(getClass().getResource(tile == 1 ? "/tiles/block1.png" : tile == 2 ? "/tiles/block2.png" : "/tiles/block3.png").toExternalForm()));
        imageTile.setFitHeight(tileSize);
        imageTile.setFitWidth(tileSize);

        imageMine = new ImageView(new Image(getClass().getResource("/tiles/mine.png").toExternalForm()));
        imageMine.setFitHeight(tileSize);
        imageMine.setFitWidth(tileSize);

        imageFlag = new ImageView(new Image(getClass().getResource("/tiles/flag.png").toExternalForm()));
        imageFlag.setFitHeight(tileSize);
        imageFlag.setFitWidth(tileSize);

        imageMisflag = new ImageView(new Image(getClass().getResource("/tiles/misflag.png").toExternalForm()));
        imageMisflag.setFitHeight(tileSize);
        imageMisflag.setFitWidth(tileSize);

        imageSure = new ImageView(new Image(getClass().getResource("/tiles/sure.png").toExternalForm()));
        imageSure.setFitHeight(tileSize);
        imageSure.setFitWidth(tileSize);

        type = 0;
        flagged = false;
        covered = true;
        setGraphic(imageTile);

    }

    public void click() {

        // Handle primary click, if is cleared or has flag, do nothing
        if (isCovered() && !isFlagged()) {

            // Hit a mine
            if (getType() == 9) {
                mine();
            } // Hit an empty tile, clear surrounding empty tiles
            else if (getType() == 0) {
                clearAroundEmpty(getRow(), getCol());
            } // Hit a tile that is in contact with a mine
            // Clear it and display the number of surrounding mines
            else {
                this.effect = new SoundEffect("fastclick",1);
                effect.start();
                setTileNumber(getType());
            }
            setCovered(false);
            setChecked(true);
        }
    }

    public void check() {
        if (!isCovered()) {
            searchSurrounding();
        }
    }

    public void tease() {
        if (isCovered() && !isFlagged()) {
            this.effect = new SoundEffect("suspense", 1);
            effect.start();
            setGraphic(getImageSure());
            setOnMouseReleased(e -> {
                if (isCovered() && !isFlagged()) {
                    setGraphic(getImageTile());
                    effect.stop();
                }
            });
        }
    }

    public void flag() {
        if (isFlagged() == true) {
            unflag();
        } else if (getGame().getMines() > 0 && isCovered()) {
            setGraphic(getImageFlag());
            setFlagged(true);
            getGame().setMines(getGame().getMines() - 1);
        }
    }

    public void unflag() {
        setFlagged(false);
        setGraphic(getImageTile());
        getGame().setMines(getGame().getMines() + 1);
    }

    public void mine() {
        setMine(true);
        setGraphic(getImageMine());
        setStyle("-fx-background-color: 'RED'; ");
        getGame().setDead(true);
        getGame().gameOver(this);
    }

    public void clear() {
        // Clear an empty tile
        // Update the number of cleared tiles
        if (getType() == 0) {
            setGraphic(null);
            getGame().setCleared(getGame().getCleared() - 1);
        } // Clear a tile with an unflagged mine
        // Sets mine field to point to this tile and isDead 
        // Timeline handles gameOver(mine)
        else if (getType() == 9 && !isFlagged()) {
            setGraphic(getImageMine());
            getGame().setDead(true);
            getGame().setMine(this);
        } // Clear a tile with an unflagged mine
        // Sets mine field to point to this tile and isDead 
        // Timeline handles gameOver(mine)
        else if (getType() != 9 && isFlagged()) {
            setGraphic(getImageMisflag());
        } // Clear a tile that is in contact with a mine
        // Update the number of cleared tiles
        else if (getType() > 0 && getType() < 9) {
            setTileNumber(getType());
        }
        setCovered(false);
        setChecked(true);
    }

    // Clear tiles around an empty clicked tile, using recursive search
    public void clearAroundEmpty(int row, int col) {
        int element = row * getCols() + col;
        Tile tile = (Tile) getGrid().getChildren().get(element);
        if (tile.isCovered()) {
            if (tile.getType() == 0) {
                tile.clear();
            }
            if (tile.getType() > 0) {
                tile.clear();
            } else {
                searchAround(row, col);
            }
        }
    }

    // Clear tiles around a cleared, non empty tile, using recursive search
    // Only searches aroung the clicked tile coordinates (row, col)
    public void clearSurrounding(int row, int col) {
        int element = row * getCols() + col;
        Tile checking = (Tile) getGrid().getChildren().get(element);
        if (!checking.isChecked()) {
            checking.clear();
        }
    }

    // Recursively search for surrounding mines
    // Take into account grid array limits
    public void searchAround(int row, int col) {
        // Search left
        if (col > 0) {
            clearAroundEmpty(row, col - 1);
        }
        // Search right
        if (col < getCols() - 1) {
            clearAroundEmpty(row, col + 1);
        }
        // Search up 
        if (row > 0) {
            clearAroundEmpty(row - 1, col);
            if (col > 0) {
                clearAroundEmpty(row - 1, col - 1);
            }
            if (col < getCols() - 1) {
                clearAroundEmpty(row - 1, col + 1);
            }
        }
        // Search down
        if (row < getRows() - 1) {
            clearAroundEmpty(row + 1, col);
            if (col > 0) {
                clearAroundEmpty(row + 1, col - 1);
            }
            if (col < getCols() - 1) {
                clearAroundEmpty(row + 1, col + 1);
            }

        }
    }

    // Recursively search for surrounding mines
    // Take into account grid array limits
    public void searchSurrounding() {
        // Search left
        if (getCol() > 0) {
            clearSurrounding(getRow(), getCol() - 1);
        }
        // Search right
        if (getCol() < getCols() - 1) {
            clearSurrounding(getRow(), getCol() + 1);
        }
        // Search up 
        if (getRow() > 0) {
            clearSurrounding(getRow() - 1, getCol());
            if (getCol() > 0) {
                clearSurrounding(getRow() - 1, getCol() - 1);
            }
            if (getCol() < getCols() - 1) {
                clearSurrounding(getRow() - 1, getCol() + 1);
            }
        }
        // Search down
        if (getRow() < getRows() - 1) {
            clearSurrounding(getRow() + 1, getCol());
            if (getCol() > 0) {
                clearSurrounding(getRow() + 1, getCol() - 1);
            }
            if (getCol() < getCols() - 1) {
                clearSurrounding(getRow() + 1, getCol() + 1);
            }

        }
    }

    /**
     * @param number the number to set
     */
    public void setTileNumber(int number) {
        setGraphic(null);
        setText(Integer.toString(getType()));
        setStyle("-fx-font-size: " + getTileSize() / 2 + "px; -fx-text-fill: hsb(" + (int) 120 / (1.5 * getType()) + ", 100%, 100%);");
        getGame().setCleared(getGame().getCleared() - 1);
    }

    /**
     * @param row the row to set
     */
    public void setRow(int row) {
        this.row = row;
    }

    /**
     * @param col the col to set
     */
    public void setCol(int col) {
        this.col = col;
    }

    /**
     * @return the covered
     */
    public boolean isCovered() {
        return covered;
    }

    /**
     * @return the flagged
     */
    public boolean isFlagged() {
        return flagged;
    }

    /**
     * @return the checked
     */
    public boolean isChecked() {
        return checked;
    }

    /**
     * @return the mine
     */
    public boolean isMine() {
        return mine;
    }

    /**
     * @return the type
     */
    public int getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * @return the row
     */
    public int getRow() {
        return row;
    }

    /**
     * @return the col
     */
    public int getCol() {
        return col;
    }

    /**
     * @return the rows
     */
    public int getRows() {
        return rows;
    }

    /**
     * @param rows the rows to set
     */
    public void setRows(int rows) {
        this.rows = rows;
    }

    /**
     * @return the cols
     */
    public int getCols() {
        return cols;
    }

    /**
     * @param cols the cols to set
     */
    public void setCols(int cols) {
        this.cols = cols;
    }

    /**
     * @return the tileSize
     */
    public double getTileSize() {
        return tileSize;
    }

    /**
     * @param tileSize the tileSize to set
     */
    public void setTileSize(double tileSize) {
        this.tileSize = tileSize;
    }

    /**
     * @return the tiles
     */
    public int[][] getTiles() {
        return tiles;
    }

    /**
     * @param tiles the tiles to set
     */
    public void setTiles(int[][] tiles) {
        this.tiles = tiles;
    }

    /**
     * @param covered the covered to set
     */
    public void setCovered(boolean covered) {
        this.covered = covered;
    }

    /**
     * @param flagged the flagged to set
     */
    public void setFlagged(boolean flagged) {
        this.flagged = flagged;
    }

    /**
     * @param checked the checked to set
     */
    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    /**
     * @param mine the mine to set
     */
    public void setMine(boolean mine) {
        this.mine = mine;
    }

    /**
     * @return the imageTile
     */
    public ImageView getImageTile() {
        return imageTile;
    }

    /**
     * @param imageTile the imageTile to set
     */
    public void setImageTile(ImageView imageTile) {
        this.imageTile = imageTile;
    }

    /**
     * @return the imageMine
     */
    public ImageView getImageMine() {
        return imageMine;
    }

    /**
     * @param imageMine the imageMine to set
     */
    public void setImageMine(ImageView imageMine) {
        this.imageMine = imageMine;
    }

    /**
     * @return the imageFlag
     */
    public ImageView getImageFlag() {
        return imageFlag;
    }

    /**
     * @param imageFlag the imageFlag to set
     */
    public void setImageFlag(ImageView imageFlag) {
        this.imageFlag = imageFlag;
    }

    /**
     * @return the imageMisflag
     */
    public ImageView getImageMisflag() {
        return imageMisflag;
    }

    /**
     * @param imageMisflag the imageMisflag to set
     */
    public void setImageMisflag(ImageView imageMisflag) {
        this.imageMisflag = imageMisflag;
    }

    /**
     * @return the imageSure
     */
    public ImageView getImageSure() {
        return imageSure;
    }

    /**
     * @param imageSure the imageSure to set
     */
    public void setImageSure(ImageView imageSure) {
        this.imageSure = imageSure;
    }

    /**
     * @return the game
     */
    public Game getGame() {
        return game;
    }

    /**
     * @param game the game to set
     */
    public void setGame(Game game) {
        this.game = game;
    }

    /**
     * @return the grid
     */
    public GridPane getGrid() {
        return grid;
    }

    /**
     * @param grid the grid to set
     */
    public void setGrid(GridPane grid) {
        this.grid = grid;
    }

}
