package com.internshala.connectfour;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Controller implements Initializable {
	private static final int col=7;
	private static final int row=6;
	private static final int dia=80;
	private static final String discCol1="#24303E";
	private static final String discCol2="#4CAA88";

	private static  String playerOne;
	private static  String playerTwo;

	private Disc[][] insertedDiscArray=new Disc[row][col];

	private boolean isPlayerOneTurn=true;

	@FXML
	public GridPane rootGridPane;
	@FXML
	public Pane discPane;
	@FXML
	public Label playerNameLabel;
	@FXML
	public TextField playerOneTextField,playerTwoTextField;
	@FXML
	public Button setNamesButton;

	private boolean isAllowedToInsert=true;


	public void createPlayground()
	{
		Platform.runLater(()->setNamesButton.requestFocus());
		Shape rectWithHoles=createGameGrid();
		rootGridPane.add(rectWithHoles,0,1);

		List<Rectangle> rectangleList=createClick();
		for (Rectangle rectangle:rectangleList) {
			rootGridPane.add(rectangle,0,1);
		}


		setNamesButton.setOnAction(event -> {
			playerOne=playerOneTextField.getText();
			playerTwo=playerTwoTextField.getText();
			playerNameLabel.setText(isPlayerOneTurn?playerOne:playerTwo);
		});

	}

	private Shape createGameGrid()
	{
		Shape rectWithHoles=new Rectangle((col+1)*dia,(row+1)*dia);

		for(int r=0;r<row;r++)
		{
			for(int c=0;c<col;c++)
			{
				Circle circle=new Circle();
				circle.setRadius(dia/2);
				circle.setCenterX(dia/2);
				circle.setCenterY(dia/2);
				circle.setSmooth(true);

				circle.setTranslateX(c*(dia+5)+dia/4);
				circle.setTranslateY(r*(dia+5)+dia/4);

				rectWithHoles=Shape.subtract(rectWithHoles,circle);
			}
		}

		rectWithHoles.setFill(Color.WHITE);
		return rectWithHoles;

	}

	private List<Rectangle> createClick(){

		List<Rectangle> rectangleList=new ArrayList<>();
		for(int c=0;c<col;c++)
		{
			Rectangle rectangle=new Rectangle(dia,(row+1)*dia);
			rectangle.setFill(Color.TRANSPARENT);
			rectangle.setTranslateX(c*(dia+5)+dia/4);

			rectangle.setOnMouseEntered(event->rectangle.setFill(Color.valueOf("#eeeeee26")));
			rectangle.setOnMouseExited(event->rectangle.setFill(Color.TRANSPARENT));

            final int column=c;
			rectangle.setOnMouseClicked(event -> {
				if(isAllowedToInsert)
				{
					isAllowedToInsert=false;
					insertDisc(new Disc(isPlayerOneTurn),column);
				}

			});

			rectangleList.add(rectangle);
		}
		return rectangleList;
	}
	private void insertDisc(Disc disc,int column)
	{
		int inRow=row-1;
		while (inRow>=0)
		{
			if (insertedDiscArray[inRow][column]==null)
				break;

			inRow--;
		}
		if(inRow<0)
			return;

		insertedDiscArray[inRow][column]=disc;
		discPane.getChildren().add(disc);
		disc.setTranslateX(column*(dia+5)+dia/4);

        int currentRow=inRow;
		TranslateTransition translateTransition=new TranslateTransition(Duration.seconds(0.5),disc);
		translateTransition.setToY(inRow*(dia+5)+(dia/4));
		translateTransition.setOnFinished(event -> {
			isAllowedToInsert=true;
			if(gameEnded(currentRow,column))
			{
				gameOver();
				return;
			}
			isPlayerOneTurn=!isPlayerOneTurn;
			playerNameLabel.setText(isPlayerOneTurn?playerOne:playerTwo);
		});
		translateTransition.play();
	}

	private boolean gameEnded(int ROW,int COLUMN)
	{
		List<Point2D> verticalPoints=IntStream.rangeClosed(ROW-3,ROW+3)
				                         .mapToObj(r->new Point2D(r,COLUMN))
				                         .collect(Collectors.toList());
		List<Point2D> horizontalPoints=IntStream.rangeClosed(COLUMN-3,COLUMN+3)
				.mapToObj(c->new Point2D(ROW,c))
				.collect(Collectors.toList());

		Point2D startPoint1=new Point2D(ROW-3,COLUMN+3);
		List<Point2D> diagonal1Points=IntStream.rangeClosed(0,6)
				.mapToObj(i->startPoint1.add(i,-i))
				.collect(Collectors.toList());

		Point2D startPoint2=new Point2D(ROW-3,COLUMN-3);
		List<Point2D> diagonal2Points=IntStream.rangeClosed(0,6)
				.mapToObj(i->startPoint2.add(i,i))
				.collect(Collectors.toList());

		boolean isEnded=checkCombination(verticalPoints) ||checkCombination(horizontalPoints)
				         || checkCombination(diagonal1Points) || checkCombination(diagonal2Points);


		return isEnded;
	}

	private boolean checkCombination(List<Point2D> points) {

		int chain=0;
		for (Point2D point:points) {
			int rowIndexForArray=(int) point.getX();
			int columnIndexForArray=(int) point.getY();

			Disc disc=getDisIfPresent(rowIndexForArray,columnIndexForArray);
			if(disc!= null && disc.isPlayerOneMove==isPlayerOneTurn)//last inserted disc belongs to current player
			{
				chain++;
				if(chain==4)
				{
					return true;
				}
			}
			else
			{
				chain=0;
			}
		}
		return false;
	}

	private Disc getDisIfPresent(int rows,int cols)     //to prevent array index out of bounds
	{
		if(rows>=row || rows<0 || cols>=col || cols<0)
			return null;

		return insertedDiscArray[rows][cols];

	}

	private void gameOver()
	{
      String winner=isPlayerOneTurn?playerOne:playerTwo;
		System.out.println("Winner is "+winner);

		Alert alert=new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Connect4");
		alert.setHeaderText("The Winner is "+winner);
		alert.setContentText("Want to play again");

		ButtonType yesBtn=new ButtonType("Yes");
		ButtonType exitBtn=new ButtonType("Exit");
		alert.getButtonTypes().setAll(yesBtn,exitBtn);

		Platform.runLater(()->{
			Optional<ButtonType>btnClicked=alert.showAndWait();

			if(btnClicked.isPresent() && btnClicked.get()==yesBtn)
			{
				resetGame();
			}else
			{
				Platform.exit();
				System.exit(0);
			}

		});

	}

	public void resetGame() {
		discPane.getChildren().clear();

		for(int r=0;r<insertedDiscArray.length;r++)
		{
			for(int c=0;c<insertedDiscArray.length;c++)
			{
				insertedDiscArray[r][c]=null;
			}
		}

		isPlayerOneTurn=true;
		playerNameLabel.setText(playerOne);
		createPlayground();
	}

	private static class Disc extends Circle
	{
		private final boolean isPlayerOneMove;

		public Disc(boolean isPlayerOneMove)
		{
			this.isPlayerOneMove=isPlayerOneMove;
			setRadius(dia/2);
			setFill(isPlayerOneMove?Color.valueOf(discCol1):Color.valueOf(discCol2)) ;
			setCenterX(dia/2);
			setCenterY(dia/2);
		}

	}



	@Override
	public void initialize(URL location, ResourceBundle resources) {

	}
}
