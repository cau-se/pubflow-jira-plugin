package de.pubflow.graph.client;



import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.StackLayoutPanel;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;

import de.pubflow.graph.common.NodeTree;
import de.pubflow.graph.common.NodePropertiesTable;
import de.pubflow.graph.common.popup.FileUploadPopup;
import de.pubflow.graph.common.popup.IconBrowserPopup;
import de.pubflow.graph.common.popup.NodeEditPopup;
import de.pubflow.graph.common.popup.RightClickMenu;
import de.pubflow.graph.common.popup.WarningPopup;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class PubFlow_GraphViewer implements EntryPoint {
	
	// Final vars
		// Strings
	private static final String GRAPH_NAME = "graph";
		private static final String FILE_MENU = "File";
		private static final String EDIT_MENU = "Edit";
		private static final String OPTIONS_MENU = "Options";
		private static final String NODES_HEADER = "Node List";
		private static final String NODE_PROPERTIES_HEADER = "Object Properties";
		private static final String ADD_NODE_TEXT = "Add Node";
		private static final String DELETE_TEXT = "Delete";
		private static final String DELETE_ALL_TEXT = "Delete All";
		private static final String DELETE_ALL_WARNING_TEXT = "Are you sure you want to delete all nodes?";
		private static final String NEW_FILE_WARNING_TEXT = "Are you sure you want to start a new Graph? All unsaved changes will be lost!";
		private static final String EDIT_TEXT = "Edit";
		private static final String CLONE_TEXT = "Clone";
		private static final String LOCK_NODES_TEXT = "Lock Graph";
		private static final String ANIMATE_TEXT = "Animations";
		private static final String MANAGE_ICONS_TEXT = "Manage Icons";
		private static final String NEW_FILE_TEXT = "New";
		private static final String OPEN_FILE_TEXT = "Open File...";
		private static final String SAVE_FILE_TEXT = "Save As...";
		private static final String SAVE_FILE_XML_TEXT = "Save As XML...";
		private static final String CHECK_SIGN = "\u2713 ";
		private static final String UNCHECK_SIGN = "\u00A0\u00A0\u00A0\u202F";
		// tooltips
		private static final String gridToggleTooltip = "Toggle Grid visibility";
		private static final String gridSnapTooltip = "Toggle Grid snapping";
		private static final String gridSmallerTooltip = "Halve Grid size";
		private static final String gridBiggerTooltip = "Double Grid size";
		private static final String zoomOutTooltip = "Zoom out (-)";
		private static final String zoomInTooltip = "Zoom in (+)";
		private static final String zoomFitTooltip = "Show entire Graph (f)";
		private static final String layoutRunTooltip = "Layout Graph";
		
		private static final String enabledColor = "black";
		private static final String disabledColor = "#888888";
		
		// doubles
		private static final double HEADER_HEIGHT = 2.5;
		private static final double BUTTON_BAR_HEIGHT = 2.4;
		private static final double SIDEBAR_WIDTH = 20;

		// Definition of the core page-elements
		private final DockLayoutPanel mainPanel = new DockLayoutPanel(Unit.EM);
		private final StackLayoutPanel leftStack = new StackLayoutPanel(Unit.EM);
		private final StackLayoutPanel rightStack = new StackLayoutPanel(Unit.EM);
		private final MenuBar topMenu = new MenuBar();
		//private final TabLayoutPanel centerPanel = new TabLayoutPanel(HEADER_HEIGHT, Unit.EM);
		//private VerticalPanel rightBasePanel = new VerticalPanel();

		// Init Menubar
		private final MenuBar fileSubMenu = new MenuBar(true);
		private final MenuBar editSubMenu = new MenuBar(true);
		private final MenuBar optionsSubMenu = new MenuBar(true);
		private MenuItem fileSaveButton;
		private MenuItem fileSaveXMLButton;
		private final MenuItem editSubMenuButton = new MenuItem(EDIT_MENU, editSubMenu);
		private final MenuItem optionsSubMenuButton = new MenuItem(OPTIONS_MENU, optionsSubMenu);

		// Definition of the SubElements
		
		private final ScrollPanel treePanel = new ScrollPanel();
		private final HTML graphPanel = new HTML("<div id='infovis' width='100%'></div>");
		
		// interface for JavaScript functions
		public final JSCaller jsCaller = new JSCaller(GRAPH_NAME, this);
		
		// right hand node properties
		final NodePropertiesTable nodePropsTable = new NodePropertiesTable(jsCaller);
		
		// left hand node list
		private final NodeTree nodeTree = new NodeTree(jsCaller);
		
		
		// icon loader popup
		private final IconBrowserPopup iconLoader = new IconBrowserPopup(jsCaller);
		
		// node editor popup
		private final NodeEditPopup nodeEditor = new NodeEditPopup(jsCaller, iconLoader);
	    
		// delete all popup
		private final WarningPopup warningPopup = new WarningPopup();
		
		// right click popup
		private final RightClickMenu rightClickMenu = new RightClickMenu(jsCaller, nodeEditor);
		
		// file upload popup
		private FileUploadPopup fileUploadPopup = new FileUploadPopup(jsCaller, this, iconLoader);
		
		// top button bar
		private VerticalPanel topPanel = new VerticalPanel();
		private HorizontalPanel buttonBar = new HorizontalPanel();
		private HorizontalPanel gridPanel = new HorizontalPanel();
		private HorizontalPanel zoomPanel = new HorizontalPanel();
		private HorizontalPanel layoutPanel = new HorizontalPanel();
		
		// add layout buttons
		private final static String PROJECT_PATH = GWT.getModuleBaseURL();
		private ToggleButton gridToggleButton = new ToggleButton(new Image(PROJECT_PATH + "images\\gridVis.png"));
		private ToggleButton gridSnapButton = new ToggleButton(new Image(PROJECT_PATH + "images\\gridSnap.png"));
		private PushButton gridBiggerButton = new PushButton(new Image(PROJECT_PATH + "images\\gridPlus.png"));
		private PushButton gridSmallerButton = new PushButton(new Image(PROJECT_PATH + "images\\gridMinus.png"));
		private PushButton zoomOutButton = new PushButton(new Image(PROJECT_PATH + "images\\zoomMinus.png"));
		private PushButton zoomInButton = new PushButton(new Image(PROJECT_PATH + "images\\zoomPlus.png"));
		private PushButton zoomFitButton = new PushButton(new Image(PROJECT_PATH + "images\\zoomFit.png"));
		//private Button layoutDirButton = new Button("ld");
		private PushButton layoutRunButton = new PushButton(new Image(PROJECT_PATH + "images\\autoLayout.png"));
		int i = 0;
	
	/**
	 * The message displayed to the user when the server cannot be reached or
	 * returns an error.
	 */
	//private static final String SERVER_ERROR = "An error occurred while "
	//		+ "attempting to contact the server. Please check your network "
	//		+ "connection and try again.";

	/**
	 * Create a remote service proxy to talk to the server-side Greeting service.
	 */
	//private final GraphServiceAsync greetingService = GWT
	//		.create(GraphService.class);

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		
		// Define EDIT - Commands
		Command cmdAddNode = new Command() {
			public void execute() {
				nodeEditor.clearFields();
				nodeEditor.center();
			}
		};
		Command cmdEditNode = new Command() {
			public void execute() {
				jsCaller.loadMarkedNodeDataNative();
				nodeEditor.setEdit(true);
				nodeEditor.center();
			}
		};
		Command cmdCloneNode = new Command() {
			public void execute() {
				jsCaller.loadMarkedNodeDataNative();
				nodeEditor.setEdit(false);
				nodeEditor.center();
			}
		};
		Command cmdDeleteNode = new Command() {
			public void execute() {
				jsCaller.loadMarkedNodeDataNative();
				nodeEditor.deleteObject();
			}
		};
		
		Command cmdDeleteAll = new Command() {
			public void execute() {
				warningPopup.setText(DELETE_ALL_WARNING_TEXT);
				warningPopup.setAcceptHandler(new ClickHandler(){
					public void onClick(ClickEvent event) {
						jsCaller.deleteAll();
						warningPopup.hide();
					}
				});
				warningPopup.center();
			}
		};
		editSubMenu.addItem(ADD_NODE_TEXT, cmdAddNode);
		editSubMenu.addSeparator();
		
		final MenuItem editNodeMI = new MenuItem(EDIT_TEXT, cmdEditNode);
		final MenuItem cloneNodeMI = new MenuItem(CLONE_TEXT, cmdCloneNode);
		final MenuItem deleteNodeMI = new MenuItem(DELETE_TEXT, cmdDeleteNode);
		editSubMenu.addItem(editNodeMI);
		editSubMenu.addItem(cloneNodeMI);
		editSubMenu.addItem(deleteNodeMI);
		editSubMenu.addSeparator();
		editSubMenu.addItem(DELETE_ALL_TEXT, cmdDeleteAll);
		
		// Define OPTIONS - Commands
		Command cmdLockNodes = null; 
		final MenuItem lockNodesItem = new MenuItem(UNCHECK_SIGN + LOCK_NODES_TEXT, cmdLockNodes);
		cmdLockNodes = new Command() {
			public void execute() {
				boolean state = jsCaller.toggleLocked();
				if(state){
					 lockNodesItem.setText(CHECK_SIGN + LOCK_NODES_TEXT);
				}else{
					lockNodesItem.setText(UNCHECK_SIGN + LOCK_NODES_TEXT);
				}
			}
		};
		lockNodesItem.setScheduledCommand(cmdLockNodes);
		
		Command cmdSetAnimation = null;
		final MenuItem animateItem = new MenuItem(UNCHECK_SIGN + ANIMATE_TEXT, cmdSetAnimation);
		cmdSetAnimation = new Command() {
			public void execute() {
				boolean state = jsCaller.toggleAnimation();
				if(state){
					animateItem.setText(CHECK_SIGN + ANIMATE_TEXT);
				}else{
					animateItem.setText(UNCHECK_SIGN + ANIMATE_TEXT);
				}
			}
		};
		
		final Command cmdManageIcons = new Command() {
			public void execute() {
				iconLoader.center();
			}
		};
		final MenuItem manageIconsItem = new MenuItem(UNCHECK_SIGN + MANAGE_ICONS_TEXT, cmdManageIcons);
		
		animateItem.setScheduledCommand(cmdSetAnimation);
		optionsSubMenu.addItem(lockNodesItem);
		optionsSubMenu.addItem(animateItem);
		optionsSubMenu.addItem(manageIconsItem);
		
		// Define FILE - Commands
		final Command cmdNewFile = new Command() {
			public void execute() {
				if(jsCaller.hasUnsavedChanges()){
					warningPopup.setText(NEW_FILE_WARNING_TEXT);
					warningPopup.setAcceptHandler(new ClickHandler(){
						public void onClick(ClickEvent event) {
							jsCaller.newGraph();
							warningPopup.hide();
						}
					});
				warningPopup.center();
				}
				else{
					jsCaller.newGraph();
					setControlsEnabled(true);
					setGraphEnabled(true);
				}
			}
		};
		final Command cmdOpenFile = new Command() {
			public void execute() {
				if(jsCaller.hasUnsavedChanges()){
					warningPopup.setText(NEW_FILE_WARNING_TEXT);
					warningPopup.setAcceptHandler(new ClickHandler(){
						public void onClick(ClickEvent event) {
							warningPopup.hide();
							fileUploadPopup.center();
						}
					});
				warningPopup.center();
				}
				else{
					fileUploadPopup.center();
				}
			}
		};
		Command cmdSaveFile = new Command() {
			public void execute() {
				jsCaller.saveGraph(false);
			}
		};
		Command cmdSaveFileXML = new Command() {
			public void execute() {
				jsCaller.saveGraph(true);
			}
		};
		fileSaveButton =  new MenuItem(SAVE_FILE_TEXT, cmdSaveFile);
		fileSaveXMLButton =  new MenuItem(SAVE_FILE_XML_TEXT, cmdSaveFileXML);
		
		// init file menu
		fileSubMenu.addItem(NEW_FILE_TEXT, cmdNewFile);
		fileSubMenu.addItem(OPEN_FILE_TEXT, cmdOpenFile);
		fileSubMenu.addItem(fileSaveButton);
		fileSubMenu.addItem(fileSaveXMLButton);
		
		// Init the menubar
		topMenu.addItem(FILE_MENU, fileSubMenu);
		topMenu.addItem(editSubMenuButton);
		topMenu.addItem(optionsSubMenuButton);
		//topMenu.addItem(ABOUT_MENU, new MenuBar());
		
		// update edit menu when clicked
		topMenu.addHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				rightClickMenu.hide();
				
				final boolean state = jsCaller.isNodeMarked();
				editNodeMI.setEnabled(state);
				cloneNodeMI.setEnabled(state);
				deleteNodeMI.setEnabled(state);
				
				// TODO : fetch from css file
				final String textColor = (state) ? enabledColor: disabledColor ;
				editNodeMI.getElement().getStyle().setColor(textColor);
				cloneNodeMI.getElement().getStyle().setColor(textColor);
				deleteNodeMI.getElement().getStyle().setColor(textColor);
			}
			
		}, ClickEvent.getType());
		
		setControlsEnabled(false);
		
		// Init the TraceTree
		nodeTree.addSelectionHandler(new SelectionHandler<TreeItem>(){
			@Override
			public void onSelection(SelectionEvent<TreeItem> event) {
				//final HTML infos = new HTML(event.getSelectedItem().getText());
			}
		});
		
		treePanel.add(nodeTree);
		treePanel.setStyleName("sidePanel", true);
		
		// Init left panel
		leftStack.add(treePanel, NODES_HEADER, HEADER_HEIGHT);
		
		// Init right panel
		rightStack.add(nodePropsTable, NODE_PROPERTIES_HEADER, HEADER_HEIGHT);

		// Init the center panel
		//centerPanel.add(graphPanel, "View Graph");
		
		// Init top buttons
		initButtonBar();
		setButtonBarEnabled(false);
		
		// Init top panel
		topPanel.setWidth("100%");
		topPanel.add(topMenu);
		buttonBar.setStyleName("gwt-MenuBar-horizontal");
		topPanel.add(buttonBar);
		
		mainPanel.addNorth(topPanel, HEADER_HEIGHT + BUTTON_BAR_HEIGHT);
		mainPanel.addWest(leftStack, SIDEBAR_WIDTH);
		mainPanel.addEast(rightStack, SIDEBAR_WIDTH);
		mainPanel.add(graphPanel);
		
		// prepare NodePropertiesTable
		mainPanel.setWidgetHidden(rightStack, true);
		nodePropsTable.initParents();
		nodePropsTable.setStyleName("sidePanel", true);
				
		// Get the rootpanel
		RootLayoutPanel rp = RootLayoutPanel.get();
		rp.add(mainPanel);
		
		jsCaller.initGraph(nodeTree, nodePropsTable, rightClickMenu);
		// RootPanel rootPanel = RootPanel.get("archiveContentPlaceHolder");
		// rootPanel.add(mainPanel);
		
		graphPanel.asWidget().getElement().getStyle().setBackgroundColor("red");
		graphPanel.setStyleName("noPadding");
		
		
		// initialize jsCaller after a short while
		Timer t = new Timer(){
			public void run(){
				cmdNewFile.execute();
			}
		};
		t.schedule(100);
	}
	
	/**
	 * Assigns standard click handlers for all buttons.
	 */
	private void addClickHandlers(){
		gridToggleButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				jsCaller.setGridVisibilityNative(gridToggleButton.isDown());
			}});
		gridSnapButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				jsCaller.setGridSnapNative(gridSnapButton.isDown());
			}});
		gridSmallerButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				jsCaller.decreaseGridSize();
			}});
		gridBiggerButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				jsCaller.increaseGridSize();
			}});
		zoomInButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				jsCaller.zoomIn();
			}});
		zoomOutButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				jsCaller.zoomOut();
			}});
		zoomFitButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				jsCaller.zoomToFitNative();
			}});
		layoutRunButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				jsCaller.autoLayoutNative();
			}});
	}
	
	/**
	 * Sets the css styles of the buttons in the top button bar.
	 */
	private void setStyles(){
		final String innerStyle = "topButtonBarInner";
		
		gridPanel.setStylePrimaryName(innerStyle);
		zoomPanel.setStylePrimaryName(innerStyle);
		layoutPanel.setStylePrimaryName(innerStyle);
		
		final String borderColor = buttonBar.getElement().getStyle().getBorderColor();
		gridPanel.getElement().getStyle().setBorderColor(borderColor);
		
		final String buttonHeight = "1.35EM";
		gridToggleButton.setHeight(buttonHeight);
		gridSnapButton.setHeight(buttonHeight);
		gridSmallerButton.setHeight(buttonHeight);
		gridBiggerButton.setHeight(buttonHeight);
		zoomOutButton.setHeight(buttonHeight);
		zoomInButton.setHeight(buttonHeight);
		zoomFitButton.setHeight(buttonHeight);
		layoutRunButton.setHeight(buttonHeight);
		
		gridToggleButton.setTitle(gridToggleTooltip);
		gridSnapButton.setTitle(gridSnapTooltip);
		gridSmallerButton.setTitle(gridSmallerTooltip);
		gridBiggerButton.setTitle(gridBiggerTooltip);
		zoomOutButton.setTitle(zoomOutTooltip);
		zoomInButton.setTitle(zoomInTooltip);
		zoomFitButton.setTitle(zoomFitTooltip);
		layoutRunButton.setTitle(layoutRunTooltip);
		/*
		gridToggleButton.setStylePrimaryName(toggleStyle);
		gridSnapButton.setStylePrimaryName(toggleStyle);
		gridSmallerButton.setStyleName(buttonStyle);
		gridBiggerButton.setStyleName(buttonStyle);
		
		zoomOutButton.setStyleName(buttonStyle, true);
		zoomInButton.setStyleName(buttonStyle);
		zoomFitButton.setStyleName(buttonStyle);
		
		layoutDirButton.setStyleName(buttonStyle);
		layoutRunButton.setStyleName(buttonStyle);
		*/
		
		// add bar layout
		//buttonBar.getElement().getStyle().setBackgroundColor("#DDDDDD");
		buttonBar.setWidth("100%");
		buttonBar.setCellWidth(gridPanel, "1");
		buttonBar.setCellWidth(zoomPanel, "1");
		//buttonBar.setCellWidth(zoomPanel, "1");
		//buttonBar.setCellWidth(gridSmallerButton, "1");
	}
	
	/**
	 * Initializes the top button bar, assigning actions to buttons and
	 * changing their layout.
	 */
	private void initButtonBar(){
		
		// add grid buttons
		buttonBar.add(gridPanel);
		gridPanel.add(gridToggleButton);
		gridPanel.add(gridSnapButton);
		gridPanel.add(gridSmallerButton);
		gridPanel.add(gridBiggerButton);
		
		// add zoom buttons
		buttonBar.add(zoomPanel);
		zoomPanel.add(zoomOutButton);
		zoomPanel.add(zoomInButton);
		zoomPanel.add(zoomFitButton);
		
		// add layout buttons
		buttonBar.add(layoutPanel);
		//layoutPanel.add(layoutDirButton);
		layoutPanel.add(layoutRunButton);
				
		// apply CSS styles
		setStyles();		
		
		// add click handlers
		addClickHandlers();
	}
	
	/**
	 * Enables / disables all buttons in the top button bar.
	 * @param state if true, enables the buttons
	 */
	public void setButtonBarEnabled(final boolean state){
		gridToggleButton.setEnabled(state);
		gridSnapButton.setEnabled(state);
		gridBiggerButton.setEnabled(state);
		gridSmallerButton.setEnabled(state);
		zoomOutButton.setEnabled(state);
		zoomInButton.setEnabled(state);
		zoomFitButton.setEnabled(state);
		layoutRunButton.setEnabled(state);
	}
	
	/**
	 * Enables Buttons and Menues, necessary for manipulating the graph.
	 * @param state if true, enables buttons
	 */
	public void setControlsEnabled(final boolean state){
		final String menuColor = (state) ? enabledColor : disabledColor;
		
		fileSaveButton.setEnabled(state);
		fileSaveButton.getElement().getStyle().setColor(menuColor);
		
		fileSaveXMLButton.setEnabled(state);
		fileSaveXMLButton.getElement().getStyle().setColor(menuColor);
		
		editSubMenuButton.setEnabled(state);
		editSubMenuButton.getElement().getStyle().setColor(menuColor);
		
		optionsSubMenuButton.setEnabled(state);
		optionsSubMenuButton.getElement().getStyle().setColor(menuColor);
		
		setButtonBarEnabled(state);
	}
	
	/**
	 * Enables some graph operations and updates its canvas.
	 * @param state if true, enables operations
	 */
	public void setGraphEnabled(final boolean state){
		jsCaller.setKeyPressEnabledNative(state);
		jsCaller.setGhostNodesEnabled(state);
		jsCaller.updateCanvasNative();
	}
	
	
	
}
