package de.pubflow.graph.common.popup;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * This extension of the PopupPanel has a fixed title bar as well
 * as a bottom Panel for buttons.
 * @author Robin Weiss
 *
 */
public class TitledPopup extends PopupPanel{
	
	private final Label title;
	private final SimplePanel content;
	private final HorizontalPanel bottomPanel;
	
	/*
	private HandlerRegistration onDragRegistration;
	private final MouseMoveHandler onDragHandler;
	private int offsetX;
	private int offsetY;
	*/
	
	public TitledPopup(final String titleText){
		super();
		super.getElement().getStyle().setPadding(0, Unit.PX);
		
		final VerticalPanel layoutPanel = new VerticalPanel();
		layoutPanel.setStyleName("popupPanel");
		
		// title
		{
			title = new Label(titleText);
			title.setStyleName("gwt-MenuBar-horizontal");
			title.setStyleName("unselectable", true);
			title.getElement().getStyle().setPaddingLeft(3, Unit.PCT);
			layoutPanel.add(title);
			
			// enable PopupDragging
			// TODO: MouseMove and MouseUp events in global area
			/*
			onDragHandler = new MouseMoveHandler(){
				@Override
				public void onMouseMove(MouseMoveEvent event) {
					final NativeEvent nEvent = event.getNativeEvent();
					final int left = nEvent.getClientX() + offsetX;
					final int top = nEvent.getClientY() + offsetY;
					setPopupPosition(left, top);
				}
			};
			title.addMouseDownHandler(new MouseDownHandler(){
				@Override
				public void onMouseDown(MouseDownEvent event) {
					final NativeEvent nEvent = event.getNativeEvent();
					offsetX = getAbsoluteLeft() - nEvent.getClientX();
					offsetY = getAbsoluteTop() - nEvent.getClientY();
					//onDragRegistration = .addHandler(onDragHandler, MouseMoveEvent.getType());
					onDragRegistration = title.addMouseMoveHandler(onDragHandler);
				}
			});
			title.addMouseUpHandler(new MouseUpHandler(){
			//.addHandler(new MouseUpHandler(){
				@Override
				public void onMouseUp(MouseUpEvent event) {
					onDragRegistration.removeHandler();
				}
			});//, MouseUpEvent.getType());
			*/
		}
		
		
		// content
		content = new SimplePanel();
		content.setStyleName("smallPadding");
		layoutPanel.add(content);
		
		bottomPanel = new HorizontalPanel();
		bottomPanel.setStyleName("popupButtonPanel");
		
		layoutPanel.add(bottomPanel);
		super.add(layoutPanel);
	}
	
	/**
	 * Sets the title text of the popup.
	 * @param titleText the new title text
	 */
	public void setTitle(final String titleText){
		title.setText(titleText);
	}
	
	/**
	 * Sets the content that goes between the title and the bottom bar.
	 * @param widget the content
	 */
	public void setContent(final Widget widget){
		content.clear();
		content.add(widget);
	}
	
	/**
	 * Adds a widget to the bottom bar.
	 */
	public void addBottomWidget(final Widget widget){
		bottomPanel.add(widget);
	}
	
}
