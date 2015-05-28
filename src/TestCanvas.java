import java.awt.Canvas;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.JLabel;


public class TestCanvas extends Canvas implements MouseMotionListener {

    private Point redPoint;
    private int redDotDiameter;
    private Point cursorPos;
    private int pointMovementSpeed;
    private int cursorWidth;
    private int cursorHeight;
    private final int LEFT = -1;
    private final int RIGHT = 1;
    private final int UP = -1;
    private final int DOWN = 1;
    private final int STILL = 0;
    private int currHorizontalDirection;
    private int currVerticalDirection;
    private JLabel displayLabel;
    private TimeLabel timeLabel;
    private JLabel percentageLabel;
    private Point topLeftOfCursor;
    private int upTime;
    private int downTime;
    private int continuousUpTime;
    private boolean isUpTimeContinuous;
    private int continuousThreshold;
    private int maximumContinuousThreshold;
    private int necessaryUpTimeToChange;
    
    
    public TestCanvas() {
        super();
        this.setBackground(Color.BLUE);
        redPoint = new Point(0,0);
        pointMovementSpeed = 3;
        currHorizontalDirection = RIGHT;
        currVerticalDirection = DOWN;
        topLeftOfCursor = new Point();
        redDotDiameter = 30;
        continuousUpTime = 0;
        isUpTimeContinuous = false;
        continuousThreshold = 60;
        maximumContinuousThreshold = 130;
        necessaryUpTimeToChange = continuousThreshold;
        
        upTime = 0;
        downTime = 0;
        setupCursor();
        setupListener();
    }

    public TestCanvas(GraphicsConfiguration arg0) {
        super(arg0);
    }

    @Override
    public void paint (Graphics g) {
        super.paint(g);
        g.setColor(Color.RED);
        g.fillOval(redPoint.x, redPoint.y, redDotDiameter, redDotDiameter);
        g.setColor(Color.YELLOW);
        g.drawRect(cursorPos.x-(cursorWidth/2), cursorPos.y-(cursorHeight/2), cursorWidth, cursorHeight);
        
     }
    
    public void setDotSpeed(int speed){
        pointMovementSpeed = speed;
    }
    
    private void setupCursor(){
    	Toolkit toolkit = Toolkit.getDefaultToolkit();
        
        //Load an image for the cursor  
          Image image = toolkit.getImage(CursorUtils.transparentPath);
          
        //Create the hotspot for the cursor  
          Point hotSpot = new Point(0,0);
          cursorWidth = 60;
          cursorHeight = 60;
          cursorPos = MouseInfo.getPointerInfo().getLocation();
          
        //Create the custom cursor  
          Cursor cursor = toolkit.createCustomCursor(image, hotSpot, CursorUtils.transparentName);
          this.setCursor(cursor);
    }
    
    //Two dimensional moving changes the x and y.
    //It is not always necessary to move in both the x and y direction.
    //Can move vertical horizontal or diagonal.
    public void moveTheDot(){
    	if(!isXInBounds(redPoint.x)){
    		changeXDirection();
    	}
    	if(!isYInBounds(redPoint.y)){
    		changeYDirection();
    	}
    	redPoint.x += pointMovementSpeed * currHorizontalDirection;
    	redPoint.y += pointMovementSpeed * currVerticalDirection; //this.getHeight()/2;
    }
    
	private boolean isXInBounds(int x) {
		if (x+(redDotDiameter) > this.getWidth()) {
			return false;
		}
		if (x < 0) {
			return false;
		}
		return true;
	}
    
    private boolean isYInBounds(int y){
    	if(y+(redDotDiameter) > this.getHeight()){
    		return false;
    	}
    	if(y<0){
    		return false;
    	}
    	return true;
    }
    
    private void changeXDirection(){
    	if(currHorizontalDirection == RIGHT){
    		currHorizontalDirection = LEFT;
    		return;
    	}
    	currHorizontalDirection = RIGHT;
    }
    
    private void changeYDirection(){
    	if(currVerticalDirection == UP){
    		currVerticalDirection = DOWN;
    		return;
    	}
    	currVerticalDirection = UP;
    }
    
    private void changeToRandomDirection(){
    	if(Math.random() < .5){
    		currVerticalDirection = UP;
    	}
    	else{
    		currVerticalDirection = DOWN;
    	}
    	if(Math.random() < .5){
    		currHorizontalDirection = LEFT;
    	}
    	else{
    		currHorizontalDirection = RIGHT;
    	}
    	
    	//Adds a chance for the dot to not be moving diagonally
    	double randomDouble = Math.random();
    	if(randomDouble <= .33){
    		currVerticalDirection = STILL;
    	}
    	else if(randomDouble <= .66){
    		currHorizontalDirection = STILL;
    	}
    }
    
    private boolean isCursorOverDot(){
    	topLeftOfCursor.x = cursorPos.x-(cursorWidth/2);
    	topLeftOfCursor.y = cursorPos.y-(cursorHeight/2);
    	int redPointCenterY = redPoint.y+redDotDiameter/2;
    	int redPointCenterX = redPoint.x+redDotDiameter/2;
    	if(redPointCenterY < topLeftOfCursor.y || redPointCenterY > (topLeftOfCursor.y + cursorHeight)){
    		return false;
    	}
    	if(redPointCenterX < topLeftOfCursor.x || redPointCenterX > (topLeftOfCursor.x + cursorWidth)){
    		return false;
    	}
    	return true;
    }
    
    private void incrementContinuousUpTime(){
    	if(isUpTimeContinuous){
    		continuousUpTime ++;
    	}
    }
    
    private boolean isContinuousUpTimeOverThreshold(){
    	return continuousUpTime > continuousThreshold;
    }
    
    private void setupListener(){
    	this.addMouseMotionListener(this);
    }
    
    public void performTestLogic(){
    	if(isCursorOverDot()){
    		isUpTimeContinuous = true;
    		upTime++;
    		if(upTime%60==0){
    			this.timeLabel.incrementTimerAndDisplay();
    		}
    		incrementContinuousUpTime();
    		incrementDisplayLabel();
    		if(isContinuousUpTimeOverThreshold() && isXInBounds(redPoint.x) && isYInBounds(redPoint.y)){
    			if(shouldDotChangeDirection()){
    				changeToRandomDirection();
    				updateNecessaryUpTimeToChange();
        			continuousUpTime = 0;
    			}
    		}
    	}
    	else{
	    	isUpTimeContinuous = false;
	    	continuousUpTime = 0;
	    	downTime++;
    	}
    	updatePercentageLabel();
    }
    
    private boolean shouldDotChangeDirection() {
    	if(continuousUpTime < necessaryUpTimeToChange) {
    		return false;
    	}
    	else {
    		return true;
    	}
    }
    
    private void updateNecessaryUpTimeToChange(){
		int setThreshDiff = (maximumContinuousThreshold - continuousThreshold);
		necessaryUpTimeToChange = (int) ( ( setThreshDiff * Math.random() ) + continuousThreshold );
		return;
    }
    
    private void incrementDisplayLabel(){
    	if(this.displayLabel != null){
    		displayLabel.setText(String.format("%d", upTime));
    	}
    }
    
    public void setDisplayLabel(JLabel label){
    	this.displayLabel = label;
    }
    
    public void setTimeLabel(TimeLabel label){
    	this.timeLabel = label;    	
    }
    
    public void setPercentageLabel(JLabel label){
    	this.percentageLabel = label;    	
    }
    
    public void updatePercentageLabel(){
    	if(this.percentageLabel != null){
    		float percentage = ((float)upTime)/(float)((upTime+downTime));
    		percentageLabel.setText(String.format("Uptime Percentage: %.0f%%", 100*percentage)); 
    	}
    }
    
	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		cursorPos = arg0.getLocationOnScreen();
		this.invalidate();
	}
}
