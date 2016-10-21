package com.example.oliverthurn.retrosquash;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.SoundPool;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.io.IOException;
import java.util.Random;

/**
 *
 * */
public class MainActivity extends AppCompatActivity {

    // Static final variables to be used throughout the class and its inner class ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    protected static final int RACKET_HEIGHT = 10;
    protected static final int NUMBER_OF_LIVES = 3;
    protected static final int RACKET_X_MOVE = 10;

    // The canvas and view objects to be used throughout the class and its inner class ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    protected Canvas canvas;
    protected SquashCourtView squashCourtView;

    // Sounds ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    private SoundPool soundPool;
    int sampleOne = -1;
    int sampleTwo = -1;
    int sampleThree = -1;
    int sampleFour = -1;

    // Variables for getting display ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    protected Display theDisplay;
    protected Point size;
    protected int screenWidth;
    protected int screenHeight;

    protected Point racketPosition;
    protected int racketWidth;
    protected int racketHeight;

    protected Point ballPosition;
    protected int ballWidth;

    // For ball movement ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    protected boolean ballIsMovingLeft;
    protected boolean ballIsMovingRight;
    protected boolean ballisMovingUp;
    protected boolean ballIsMovingDown;

    // Racket movement ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    protected boolean racketIsMovingLeft;
    protected boolean racketIsMovingRight;

    // Stats ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    protected long lastFrameTime;
    protected int fps;
    protected int score;
    protected int lives;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        squashCourtView = new SquashCourtView(this);
        setContentView(squashCourtView);

        // Getting sounds
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);

            try{
                AssetManager assetManager = getAssets();
                AssetFileDescriptor descriptor;

                // Creating the sounds in memory
                descriptor = assetManager.openFd("soundone.wav");
                sampleOne = soundPool.load(descriptor, 0);

                descriptor = assetManager.openFd("soundtwo.wav");
                sampleTwo = soundPool.load(descriptor, 0);

                descriptor = assetManager.openFd("soundthree.wav");
                sampleThree = soundPool.load(descriptor, 0);

                descriptor = assetManager.openFd("soundfour.wav");
                sampleFour = soundPool.load(descriptor, 0);

                } catch (IOException e){

                    e.printStackTrace();
            }

        // Getting the device screen size
        theDisplay = getWindowManager().getDefaultDisplay();
        size = new Point();
        theDisplay.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;
        Log.i("MyInfo", "screen width"+ screenWidth);
        Log.i("MyInfo", "screen height"+ screenHeight);


        // Setting the game objects in default sizes and positions
        racketPosition = new Point();
        racketPosition.x = screenWidth / 2;
        racketPosition.y = screenHeight - 20;
        racketWidth = screenWidth / 8;
        racketHeight = RACKET_HEIGHT;

        ballWidth = screenWidth / 35;
        ballPosition = new Point();
        ballPosition. x = screenWidth / 2;
        ballPosition.y = 1 + ballWidth;

        lives = NUMBER_OF_LIVES;

    }

    // Start of the inner class SquashCourtView that will do everything we want the game to do ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public class SquashCourtView extends SurfaceView implements Runnable {

        // Creating the thread, canvas holder, paint object and the boolean whether to play the game in the run method.
        Thread runThread = null;
        volatile boolean gameOn;
        SurfaceHolder holder;
        Paint canvasPaint;

        public SquashCourtView(Context context){

            super(context);
            holder = getHolder();
            canvasPaint = new Paint();
            ballIsMovingDown = true;
            Log.i("MyInfo", "ballIsMovingDown"+ ballIsMovingDown);


            // Send the ball in random direction
            Random randomNumber = new Random();
            int ballDirection = randomNumber.nextInt(3);
            Log.i("MyInfo", "ballDirection"+ ballDirection);

            // Set Parent class booleans for what direction the ball is moving. To be used in the onTouchButton
             switch (ballDirection) {

                 case 0:
                    ballIsMovingLeft = true;
                    ballIsMovingRight = false;
                    break;
                case 1:
                    ballIsMovingLeft = false;
                    ballIsMovingRight = true;
                    break;
                 case 2:
                    ballIsMovingLeft = false;
                    ballIsMovingRight = false;
                    break;
                 default:
                    break;
             }
        }


        // While the method runs, update the court, redraw the court and make the game run smoothly from device to device
        // by controlling the FPS
        @Override
        public void run() {

            while (gameOn) {
                updateCourt();
                drawCourt();
                controlFPS();
                //Log.i("MyInfo", "in run thread");
                Log.i("MyInfo", "ballpos.x = " + ballPosition.x);
                Log.i("MyInfo", "ballpos.y = " + ballPosition.y);
                Log.i("MyInfo", "racketpos.x = " + racketPosition.x);
                Log.i("MyInfo", "racketpos.y = " + racketPosition.y);
                Log.i("MyInfo", "racketWidth = " + racketWidth);
                Log.i("MyInfo", "racketHeight = " + racketHeight);
                Log.i("MyInfo", "screenWidth = " + screenWidth);
                Log.i("MyInfo", "screenHeight = " + screenHeight);
                Log.i("MyInfo", "ballWidth = " + ballWidth);

            }

        }

        /** public void updateCourt()
         *
         * Method will be called while the SquashCourtView classes run method is being called
         * will update the screen with new positions of the racket and ball, while detecting
         * collision with the outer edges of the game screen and knowing when the player has
         * lost a life or the score needs to be incremented also detecting and telling the
         * racket which way to move when being pressed, and sending the ball on the correct
         * path determined by what part of the screen it last came in contact with and or
         * what part of the racket it landed on
         *
         * */

        public void updateCourt(){

            if (racketIsMovingRight){

                racketPosition.x = racketPosition.x + RACKET_X_MOVE;
                Log.i("MyInfo", "inside racket move right");
            }
            if(racketIsMovingLeft){

                racketPosition.x = racketPosition.x - RACKET_X_MOVE;
            }

            // Detect a collision with the right side of screen
            if (ballPosition.x + ballWidth > screenWidth){
                ballIsMovingLeft = true;
                ballIsMovingRight = false;
                //soundPool.play(sampleOne, 1, 1, 0, 0, 1);
            }

            // Detect a collision with the left side of the screen
            if (ballPosition.x < 0){
                ballIsMovingLeft = false;
                ballIsMovingRight = true;
                //soundPool.play(sampleOne, 1, 1, 0, 0, 1);
            }

            // Detect if the ball has gone off the bottom of the screen ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            if (ballPosition.y > screenHeight - ballWidth) {
                lives = lives - 1;

                if (lives == 0) {
                    lives = 3;
                    score = 0;
                    //soundPool.play(sampleFour, 1, 1, 0, 0,1);
                }

                ballPosition.y = 1 + ballWidth;

                // Setting a direction for when the ball comes back onto the screen
                Random randomNumber = new Random();
                int startX = randomNumber.nextInt(screenWidth - ballWidth) + 1;
                ballPosition.x = startX + ballWidth;

                // Repeating the switch for what direction to go depending on the random number generated.
                int ballDirection = randomNumber.nextInt(3);

                switch (ballDirection) {
                    case 0:
                        ballIsMovingLeft = true;
                        ballIsMovingRight = false;
                        break;
                    case 1:
                        ballIsMovingLeft = false;
                        ballIsMovingRight = true;
                        break;
                    case 2:
                        ballIsMovingLeft = false;
                        ballIsMovingRight = false;
                        break;
                    default:
                        break;
                }
            }

                // Detect if the ball has reached the top of the screen ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                if (ballPosition.y <= 0){
                    ballIsMovingDown = true;
                    ballisMovingUp = false;
                    ballPosition.y = 1;
                    // soundPool.play(sampleTwo, 1, 1, 0, 0, 1);
                }

                if (ballIsMovingDown){
                    ballPosition.y += 6;
                    Log.i("MyInfo", "Inside ball is moving");
                }

                if (ballisMovingUp){
                    ballPosition.y -= 10;
                }

                if (ballIsMovingLeft){
                    ballPosition.x -= 12;
                }

                if (ballIsMovingRight){
                    ballPosition.x += 12;
                }

                // Detect if the ball has hit the racket ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                if (ballPosition.y + ballWidth >= (racketPosition.y - racketHeight / 2)){
                    int halfRacket = racketWidth / 2;

                    if (ballPosition.x + ballWidth > (racketPosition.x - halfRacket) &&
                            ballPosition.x - ballWidth < (racketPosition.x + halfRacket)){

                        // Update the score
                        score++;

                        // Bounce the ball back Vertically
                        ballisMovingUp = true;
                        ballIsMovingDown = false;

                        //soundPool.play(sampleThree, 1, 1, 0, 0, 1);

                            if (ballPosition.x > racketPosition.x){
                                ballIsMovingRight = true;
                                ballIsMovingLeft = false;

                            }   else {
                                    ballIsMovingRight = false;
                                    ballIsMovingLeft = true;

                            }

                    }

                }


            //Log.i("MyInfo", "end of update()");

        } // End of update().

        /** public void drawCourt()
         *
         *
         * */

        public void drawCourt(){
           // Log.i("MyInfo", "in drawCourt()");


                if (holder.getSurface().isValid()) {
                    canvas = holder.lockCanvas();
                    //Paint paint = new Paint();
                    canvas.drawColor(Color.BLACK);
                    canvasPaint.setColor(Color.WHITE);
                    canvasPaint.setTextSize(45);

                    Paint racketGreen = new Paint();
                    racketGreen.setColor(Color.GREEN);

                    Paint ballWhite = new Paint();
                    ballWhite.setColor(Color.WHITE);

                    //canvas.drawCircle(racketPosition.x, racketPosition.y, 100, green);
                    canvas.drawText("Score:" + score + " Lives: " + lives + " FPS:" + fps, 20, 40, canvasPaint);


                    // Draw the racket ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    canvas.drawRect(racketPosition.x - (racketWidth / 2), racketPosition.y - racketHeight, racketPosition.x + (racketWidth / 2), racketPosition.y , racketGreen);

                    // Draw the ball ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    canvas.drawRect(ballPosition.x, ballPosition.y, ballPosition.x + ballWidth, ballPosition.y + ballWidth, ballWhite);

                    // Setting the canvas onto the screen ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    holder.unlockCanvasAndPost(canvas);
                    //Log.i("MyInfo", "passed unlock and post");
                }
           // Log.i("MyInfo", "out of drawCourt() surface wasnt valid?");



        } // End of drawCourt().

        public void controlFPS(){

            long timeThisFrame = (System.currentTimeMillis() - lastFrameTime);
            long timeToSleep = 10 - timeThisFrame;

            if (timeThisFrame > 0){
                fps = (int) (1000 / timeThisFrame);

            }

            if (timeToSleep > 0){

                try {
                    runThread.sleep(timeToSleep);

                } catch (InterruptedException e){
                    e.printStackTrace();

                }
            }

            lastFrameTime = System.currentTimeMillis();

        } // End of controlFPS().

        public void pause(){

            gameOn = false;
            try {
                runThread.join();
            } catch (InterruptedException e){
                e.printStackTrace();

            }
        } // End of pause().

        public void resume(){
            Log.i("MyInfo", "thread is resumed");
            gameOn = true;
            runThread = new Thread(this);
            runThread.start();

        } // End of resume().

        @Override
        public boolean onTouchEvent(MotionEvent event) {

            Log.i("MyInfo", "a touch happened");
            switch (event.getAction()){

                case MotionEvent.ACTION_DOWN:

                    if (event.getX() >= screenWidth / 2){
                        racketIsMovingRight = true;
                        racketIsMovingLeft = false;

                    } else {
                        racketIsMovingLeft = true;
                        racketIsMovingRight = false;

                    }
                    break;
                case MotionEvent.ACTION_UP:
                    racketIsMovingRight = false;
                    racketIsMovingLeft = false;
                    break;
                default:
                    break;

            }
            return true;
            //return super.onTouchEvent(event);
        }
    } // End of SquashCourtView Class ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


    @Override
    protected void onPause() {
        super.onPause();
        squashCourtView.pause();

    }

    @Override
    protected void onResume() {
        super.onResume();
        squashCourtView.resume();
    }

    @Override
    protected void onStop() {
        super.onStop();

        while (true){
            squashCourtView.pause();
            break;

        }
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if ( keyCode == KeyEvent.KEYCODE_BACK){
            squashCourtView.pause();
            finish();
            return true;
        }
        return false;

    }
} // End of MainActivity Class ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
