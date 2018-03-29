/*
 * Helen Zhang
 * SpaceMenu.java
 *
 * This is the menu class containing the start screen. It calls the SpaceIvaders class to start the main game.
 */

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Random;
public class SpaceMenu extends JFrame implements ActionListener{
	
	SpaceInvaders mainGame;
	Image back,endGame;
	JButton play;
	boolean enter;
	
	// Constructor. Set up background images and create and add the "PLAY" button
	public SpaceMenu(int result){		
		super("Space Invaders Menu");		
		setSize(800,600);
		setResizable(false);
		back = new ImageIcon("menuScreen.png").getImage();			// menuScreen has all the actual words and whatnot that don't
		setLayout(null);											// need to be edited
		
		play = new JButton("PLAY");
    	play.addActionListener(this);
    	play.setSize(100,30);
    	play.setLocation(350,450);
    	add(play);
    	getRootPane().setDefaultButton(play);					
    	
		setVisible(true);											// this starts off as true because SpaceMenu calls SpaceInvaders instead of the other
	}																// way around.
	
	// Action performed - If the play button is clicked, or if any key is pressed, call SpaceInvaders and make this window invisible
	public void actionPerformed(ActionEvent evt){
	 	Object source = evt.getSource();

		if(source==play){
			setVisible(false);
			new SpaceInvaders();			
		}
	}   
	
	// Graphics for the sake of graphics - all it does is draw the background image if it's not null
	public void paint(Graphics g){
		if(back!=null){
			g.drawImage(back,0,0,this);
		}
	}
	
	// Main because this class is the starting point
	public static void main(String[]args){
    	SpaceMenu space=new SpaceMenu(0);
    }
}

