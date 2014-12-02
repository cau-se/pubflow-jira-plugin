package de.pubflow.graph.common;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;

public class Notification extends PopupPanel{
	private final Label text;
	private final Timer timer;
	private int state;
	private int remainingTime;
	
	private static Notification instance;
	
	private Notification(){
		super();
		super.setStyleName("notificationBox");
		
		// add message label
		text = new Label("");
		text.setStyleName("unselectable", true);
		super.add(text);
		
		state = -1;
		
		// init timer
		timer = new Timer() {
            @Override
            public void run() {
            	switch(state){
            		case 0:{
            			if(remainingTime == 0){
            				hide();
                    		// add half a second delay for equal messages
                    		timer.schedule(500);
                    		state = 2;
            			}else{
	            			setAutoHideEnabled(true);
	            			timer.schedule(remainingTime);
	            			state = 1;
            			}
            			return;
            		}
            		case 1:{
            			hide();
                		// add half a second delay for equal messages
                		timer.schedule(500);
                		state = 2;
                		return;
            		}
            		case 2:{
            			text.setText("");
            			state = -1;
            			return;
            		}
            	}
            }
        };
	}
	
	public static void display(final String message, final int duration){
		if(instance == null){
			instance = new Notification();
		}
		
		// if the same message is already shown, ignore the call
		if(message.equals(instance.text.getText())){
			return;
		}

		instance.setAutoHideEnabled(false);
		instance.text.setText(message);
		instance.timer.schedule(500);
		instance.center();
		
		instance.remainingTime = Math.max(0, duration - 500);
		instance.state = 0;
	}
}
