package de.pubflow.graph.common;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * A quadratic SimplePanel containing an Image Object. The Panel does not scale with its content,
 * instead the Image is scaled to fit the panel
 * @author Robin Weiss
 *
 */
public class ImageHolder extends SimplePanel{
	
	private final Image image;
	
	/**
	 * Constructor the defines the size of the ImageHolder
	 * @param size
	 */
	public ImageHolder(final int size){
		super();

		// define basic style
		setStyleName("iconHolder");
		final Style style = getElement().getStyle();
		style.setWidth(size, Unit.PX);
		style.setHeight(size, Unit.PX);
		style.setProperty("maxWidth", size, Unit.PX);
		style.setProperty("maxHeight", size, Unit.PX);
		
		image = new Image();
		image.setStyleName("unselectable", true);
		image.setVisible(false);
		add(image);
		
		// resizes the image to an acceptable size in correct proportions
	    image.addLoadHandler(new LoadHandler(){
			@Override
			public void onLoad(LoadEvent event) {
				
				final int iWidth = image.getWidth();
				final int iHeight = image.getHeight();
				
				if(iWidth > iHeight){
					final int newHeight = (iHeight * size / iWidth);
					image.setSize(size + "px", newHeight + "px");
					final int padding = (size - newHeight) / 2;
					image.getElement().getStyle().setMarginTop(padding, Unit.PX);
				}
				else{
					image.setSize((iWidth * size / iHeight) + "px", size + "px");
					image.getElement().getStyle().setMarginTop(0, Unit.PX);
				}
				image.setVisible(true);
			}
	    });
	}
	
	/**
	 * 
	 * @return true if the image is visible and thus was successfully loaded.
	 */
	public boolean hasValidImage(){
		return image.isVisible();
	}
	
	/**
	 * 
	 * @return the stored image
	 */
	public Image getImage(){
		return image;
	}
	
	/**
	 * Restores the size of the image and loads the url of
	 * the other image.
	 * @param other a valid image or null
	 */
	public void setImage(final Image other){
		// make the image invisible while loading
		image.setVisible(false);
		
		if(other != null){
			// get necessary data from other image
			final int width = other.getWidth();
			final int height = other.getHeight();
			
			image.setWidth(width + "px");
			image.setHeight(height + "px");
			image.setUrl("");
			image.setUrl(other.getUrl());
		}
	}
}
