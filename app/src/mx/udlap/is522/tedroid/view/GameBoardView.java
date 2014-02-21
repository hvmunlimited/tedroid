package mx.udlap.is522.tedroid.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import mx.udlap.is522.tedroid.view.model.Tetromino;

import java.util.ArrayList;
import java.util.List;

/**
 * Tablero del juego donde los tetrominos se acumlan.
 * 
 * @author Daniel Pedraza-Arcega
 * @since 1.0
 */
public class GameBoardView extends View {

    private static final float MOVE_SENSITIVITY = 3.5f;
    private static final int DEFAULT_DIMENSIONS = 18;
    private static final long DEFAULT_SPEED = 500l;
    
    // TODO: Quitar esto cuando ya no estemos en desarrollo
    private boolean isUnderDebuging = true;
    
    private List<Tetromino> tetrominos;
    private Tetromino currentTetromino;
    private int[][] boardMatrix;
    private long speed;
    private float width;
    private float height;
    private boolean startDropingTetrominos;
    private boolean isPaused;
    private GestureDetector gestureDetector;
    private MoveDownCurrentTetrominoTask moveDownCurrentTetrominoTask;
    private Paint background;
    private Paint verticalGridLineColor;
    private Paint horizontalGridLineColor;

    /**
     * Construye un tablero de juego.
     * @see android.view.View#View(Context)
     */
    public GameBoardView(Context context) {
	super(context);
	setUpLayout();
    }

    /**
     * Construye un tablero de juego mediante XML
     * @see android.view.View#View(Context, AttributeSet)
     */
    public GameBoardView(Context context, AttributeSet attrs) {
	super(context, attrs);
	setUpLayout();
    }

    /**
     * Construye un tablero de juego mediante XML y aplicando un estilo.
     * @see android.view.View#View(Context, AttributeSet, int)
     */
    public GameBoardView(Context context, AttributeSet attrs, int defStyleAttr) {
	super(context, attrs, defStyleAttr);
	setUpLayout();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
	super.onSizeChanged(w, h, oldw, oldh);
	width = w / ((float) boardMatrix[0].length);
	height = h / ((float) boardMatrix.length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBackground(canvas);
        drawTetrominos(canvas);

        if (isUnderDebuging) {
            drawGrid(canvas);
        }
        
        if (!startDropingTetrominos) {
            startDropingTetrominos = true;
            moveDownCurrentTetrominoTask = new MoveDownCurrentTetrominoTask();
            moveDownCurrentTetrominoTask.execute(speed);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
	gestureDetector.onTouchEvent(event);
	switch (event.getAction()) {
	    case MotionEvent.ACTION_DOWN:
		return true;
	    default: return super.onTouchEvent(event);
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void invalidate() {
	if (!isPaused) super.invalidate();
    }

    /**
     * Inicializa el layout de este tablero.
     */
    protected void setUpLayout() {
	// TODO: Crear matrix por medio de setters o atributo xml
	boardMatrix = new int[DEFAULT_DIMENSIONS][DEFAULT_DIMENSIONS];
	
	// TODO: Inicializar velocidad del juego por medio de setters o atributo xml
	speed = DEFAULT_SPEED;
	
	gestureDetector = new GestureDetector(getContext(), new GestureListener());
	tetrominos = new ArrayList<Tetromino>();
	currentTetromino = new Tetromino.Builder(this)
        	.useRandomDefaultShape()
        	.build();
        tetrominos.add(currentTetromino);
        isPaused = false;
        
	// TODO: Hardcoded
	background = new Paint();
	background.setColor(0xff000000);
	verticalGridLineColor = new Paint();
	verticalGridLineColor.setColor(0xff0000ff);
	horizontalGridLineColor = new Paint();
	horizontalGridLineColor.setColor(0xffff0000);
    }

    /**
     * Dibuja el fondo del tablero.
     * 
     * @param canvas un canvas para dibujar.
     */
    protected void drawBackground(Canvas canvas) {
	canvas.drawRect(0, 0, getWidth(), getHeight(), background);
    }

    /**
     * Dibuja la cuadrilla del tablero.
     * 
     * @param canvas un canvas para dibujar.
     */
    protected void drawGrid(Canvas canvas) {
	// Vertical grid lines
	for (int i = 0; i < boardMatrix[0].length; i++) {
	    canvas.drawLine(i * width, 0, i * width, getHeight(), verticalGridLineColor);
	}
	// Horizontal grid lines
	for (int i = 0; i < boardMatrix.length; i++) {
	    canvas.drawLine(0, i * height, getWidth(), i * height, horizontalGridLineColor);
	}
    }

    /**
     * Dibuja todos los tetrominos que se han acumulado.
     * 
     * @param canvas un canvas para dibujar.
     */
    protected void drawTetrominos(Canvas canvas) {
	for (Tetromino t : tetrominos) {
	    t.drawOnParentGameBoardView(canvas);
	}
    }

    /**
     * Actualiza la matriz del tablero con los valores del tetromino actual.
     */
    protected void updateBoardMatrix() {
	for (int row = 0; row < currentTetromino.getShapeMatrix().length; row++) {
	    for (int column = 0; column < currentTetromino.getShapeMatrix()[0].length; column++) {
		int boardMatrixRow = currentTetromino.getPositionOnBoard().getY() + row;
		int boardMatrixColumn = currentTetromino.getPositionOnBoard().getX() + column;
		boardMatrix[boardMatrixRow][boardMatrixColumn] = currentTetromino.getShapeMatrix()[row][column];
	    }
	}
    }

    /**
     * @return la matriz del tablero.
     */
    public int[][] getBoardMatrix() {
	return boardMatrix;
    }

    /**
     * @return la altura del tablero.
     */
    public float getBoardHeight() {
	return height;
    }

    /**
     * @return la anchura del tablero.
     */
    public float getBoardWidth() {
	return width;
    }

    /**
     * Pausa el juego.
     */
    public void pauseGame() {
	isPaused = true;
    }

    /**
     * Reanuda el juego.
     */
    public void resumeGame() {
	isPaused = false;
    }

    /**
     * Detiene el juego y no se podrá reinciar más
     */
    public void stopGame() {
	if (moveDownCurrentTetrominoTask != null && moveDownCurrentTetrominoTask.getStatus() == AsyncTask.Status.RUNNING) {
	    moveDownCurrentTetrominoTask.cancel(true);
	    moveDownCurrentTetrominoTask = null;
	}
    }

    /**
     * Tarea que lleva la cuenta de la velocidad de caida del tetromino en
     * juego.
     * 
     * @author Daniel Pedraza-Arcega
     * @since 1.0
     */
    private class MoveDownCurrentTetrominoTask extends AsyncTask<Long, Void, Void> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Void doInBackground(Long... params) {
	    while (!isCancelled()) {
		try {
		    Thread.sleep(params[0]);
		    publishProgress();
		} catch (InterruptedException e) { }
	    }
	    
	    return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onProgressUpdate(Void... values) {
	    if (!currentTetromino.moveDown()) {
		Log.d("Tetris", "New randow tetromino");
		updateBoardMatrix();
		currentTetromino = new Tetromino.Builder(GameBoardView.this)
			.useRandomDefaultShape()
			.build();
		tetrominos.add(currentTetromino);
	    } else {
		 Log.d("Tetris", "Move down tetromino");
	    }
	    invalidate();
	}
    }

    /**
     * Escucha los eventos del tablero para mover el tetromino en juego.
     * 
     * @author Daniel Pedraza-Arcega
     * @since 1.0
     */
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
	    if (distanceX < -MOVE_SENSITIVITY) {
		Log.d("Tetris", "Move tetromino to the right");
		currentTetromino.moveTo(Tetromino.Direction.RIGHT);
		invalidate();
		return true;
	    } else if (distanceX > MOVE_SENSITIVITY) {
		Log.d("Tetris", "Move tetromino to the left");
		currentTetromino.moveTo(Tetromino.Direction.LEFT);
		invalidate();
		return true;
	    }
	    return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onSingleTapUp(MotionEvent e) {
	    Log.d("Tetris", "Rotate tetromino");
	    currentTetromino.rotate();
	    invalidate();
	    return true;
	}
    }
}