package sample;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import java.io.File;



public class GamePane extends BorderPane {

    // Media player for soundtrack (for some reasons it won't work if its not global)
    private final MediaPlayer mediaPlayer;

    // soundtrack url
    private static final String media_url = "soundtrack/music.mp3";

    int gamePaneWidth = 940;
    int gamePaneHeight = 600;

   // Create the ball and its properties
    private final  Circle ball;
    private double ballX;
    private double ballY;
    private double radius;
    private double dx = 1, dy = 1;
    // TODO max 1.5


    // Properties for a brick
    private  final double rectangleWidth = 80;
    private  final double rectangleHeight = 40;
    // gap between bricks
    private final int gap = 20;

    // moving ball animation
    private final Timeline animation;

    // that rectangle thing that hits the ball
    private Rectangle blade;
    private final  int bladeHeight = 20;
    private final int bladeWidth = 300;

    // Number of bricks rows and columns
    private final int rows = 3;
    private final int cols = 9;

    // Number of total bricks
    private int numberOfBricks;

    // Make the blade a unique rectangle
    private final String bladeId = "do not remove me";

    // Player's score
    private int score = 0;

    // pane where game's components are located
    private Pane playTable;

    // top zone for score, lives
    // TODO lives
    private HBox scorePane;
    private Text scoreText;

    /** Default constructor */
    public GamePane() {

        // Add Soundtrack
        Media media = new Media(new File(media_url).toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setVolume(30);
        mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        mediaPlayer.play();


        // Set the total width and height of the main pane
        setWidth(gamePaneWidth);
        setHeight(gamePaneHeight);

        // Create pane for bricks, ball and blade
        playTable = new Pane();
        playTable.setBorder(new Border(new BorderStroke(Color.WHITE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        playTable.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
        setCenter(playTable);

        // Create pane for score, lives
        scorePane = new HBox(5);
        scorePane.setBorder(new Border(new BorderStroke(Color.WHITE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        scorePane.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));

        Text scoreLabelText = new Text();
        scoreLabelText.setText("SCORE: ");
        scoreLabelText.setFont(Font.font("Arial", FontWeight.MEDIUM, FontPosture.REGULAR, 20));
        scoreLabelText.setFill(Color.RED);
        scorePane.getChildren().add(scoreLabelText);

        //Actual score indicator
        scoreText = new Text();
        scoreText.setFont(Font.font("Arial", FontWeight.MEDIUM, FontPosture.REGULAR, 20));
        scoreText.setFill(Color.YELLOW);
        scoreText.setText("0");
        scorePane.getChildren().add(scoreText);

        // Set the score pane at the top of the main pane
        setTop(scorePane);

        // Calculate the total number of bricks
        numberOfBricks = rows * cols;

        // Generate bricks model
        createBricks(rows, cols);

        // Set ball properties
        ballX = gamePaneWidth / 2;
        ballY = gap + (gap + rectangleHeight) * rows + radius + 10;
        radius = 20;
        ball = new Circle(ballX, ballY, radius);
        ball.setFill(Color.AQUA);

        // Create the moving animation for the ball
        animation = new Timeline(new KeyFrame(Duration.millis(50), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                moveBall();
            }
        }));
        animation.setCycleCount(Timeline.INDEFINITE);
        animation.setDelay(Duration.millis(50));
        animation.setRate(10);

        // When clicked on pane, the ball will spawn and the game will start
        setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if(animation.getStatus() == Animation.Status.STOPPED) {
                    playTable.getChildren().add(ball);
                    animation.play();
                }
            }
        });

        // Set blade properties
        blade = new Rectangle();
        blade.setX(20);
        blade.yProperty().bind(heightProperty().subtract(50));
        blade.setId(bladeId);
        blade.setHeight(bladeHeight);
        blade.setWidth(bladeWidth);
        blade.setFill(Color.DARKGREY);
        playTable.getChildren().add(blade);


        // Move blade based on the key pressed
        // D for right
        // A for left
        this.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if(keyEvent.getCode() == KeyCode.D)
                    moveRight();
                if(keyEvent.getCode() == KeyCode.A)
                    moveLeft();
            }
        });


    }


    // Move the blade to the left with 40 pixels
    private void moveLeft() {
        if(blade.getX() > 0 )
        blade.setX(blade.getX() - 40);
    }

    // Move the blade to the right with 40 pixels
    private void moveRight() {
        if(blade.getX() + bladeWidth < getWidth())
        blade.setX(blade.getX() + 40);
    }

    // Play the moving ball animation
    public void play(){
        animation.play();
    }
    // Stop the moving ball animation
    public void pause(){
        animation.pause();
    }


    /**
     * Physics for interaction between ball and bricks/blade/pane
     */
    private void moveBall() {
        // Search through all nodes and select those who are rectangles
        for(Object obj : playTable.getChildren().toArray()) {
            if (obj instanceof Rectangle) {
                // Save its coordinates
                double rectangleX = ((Rectangle) obj).getX();
                double rectangleY = ((Rectangle) obj).getY();
                // If ball intersects a rectangle
                if (ball.getBoundsInLocal().intersects(((Rectangle) obj).getBoundsInLocal())) {
                    // Check if the rectangle is the blade
                    if (bladeId.equals(((Rectangle) obj).getId())) {

                        // Blade interaction physics
                        if(ball.getCenterX() < rectangleX + bladeWidth / 2){
                            double contactPoint = ball.getCenterX() - bladeWidth;
                            double percent = contactPoint * 100 / bladeWidth / 2;
                            dx = -(100 - percent)/100 * 1.5;
                            //animation.setRate(6.6);
                        }
                        if(ball.getCenterX() > rectangleX + bladeWidth / 2){
                            double contactPoint = rectangleX + bladeWidth - ball.getCenterX();
                            double percent = (contactPoint * 100) / (bladeWidth / 2);
                            dx = (100 - percent)/100 * 1.5;
                            System.out.println("Dx: " + dx + " RectangleX: " + rectangleX + " Contact point: " + contactPoint + "Percent: " + percent + "Rectangle X + width: " + rectangleWidth);

                        }
                        if(ball.getCenterX() == rectangleX)
                            dx = 0;
                        dy *= -1;
                        continue;
                    }
                    // Bricks physics
                    if (ball.getCenterX() + radius >= rectangleX || ball.getCenterX() - radius <= rectangleX + rectangleWidth)
                        dx *= -1;
                    if (ball.getCenterY() + radius >= rectangleY || ball.getCenterY() - radius <= rectangleY + rectangleHeight)
                        dy *= -1;

                    // Remove the brick that the ball interacted with
                    playTable.getChildren().remove(obj);

                    // Add score for every brick removed
                    score += 5;
                    scoreText.setText(score + "");
                    // When there is no bricks left, the player wins the game
                    if (--numberOfBricks <= 0)
                        gameWon();
                }
            }
        }

        // Pane physics
        if(ballX < radius || ballX > playTable.getWidth() - radius)
            dx *=  -1;
        if(ballY < radius )
            dy *= -1;
        // If the ball falls, the game is over
        if(ballY > playTable.getHeight() - radius)
            gameOver();


        ballX += dx;
        ballY += dy;
        ball.setCenterX(ballX);
        ball.setCenterY(ballY);
    }


    // Stop the animation and show a message when the player wins the game
    private void gameWon() {
        pause();
        Text text = new Text("Congratulations! You win!");
        text.setFill(Color.RED);
        text.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
        text.setX(320);
        text.layoutYProperty().bind(heightProperty().divide(2));
        playTable.getChildren().add(text);
        playTable.getChildren().add(new Text(300, 460, "You score: " + score));

    }

    // Stop the animation and show a message when the player fails the game
    private void gameOver() {
        pause();
        Text text = new Text("Game Over");
        text.setFill(Color.RED);
        text.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
        text.setX(320);
        text.layoutYProperty().bind(heightProperty().divide(2));
        getChildren().add(text);
    }

    /**
     * Create bricks model
     * @param row number of bricks rows
     * @param col number of bricks columns
     */
    private void createBricks(int row, int col) {

        for(int i = 0 ; i < row; i ++)
            for(int j = 0; j < col ; j ++){
                Rectangle rectangle = new Rectangle();
                rectangle.setX( gap + j * (rectangleWidth + gap));
                rectangle.setY( gap + i * (gap + rectangleHeight));
                rectangle.setHeight(rectangleHeight);
                rectangle.setWidth(rectangleWidth);
                rectangle.setFill(Color.WHITE);
                rectangle.setStroke(Color.RED);
                playTable.getChildren().add(rectangle);
            }

    }
}
