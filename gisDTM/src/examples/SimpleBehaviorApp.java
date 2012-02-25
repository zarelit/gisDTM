/*
 *      @(#)SimpleBehaviorApp.java 1.1 00/09/22 16:24
 *
 * Copyright (c) 1996-2000 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,
 * modify and redistribute this software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) Licensee does not utilize the software in a manner
 * which is disparaging to Sun.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * This software is not designed or intended for use in on-line control of
 * aircraft, air traffic, aircraft navigation or aircraft communications; or in
 * the design, construction, operation or maintenance of any nuclear
 * facility. Licensee represents and warrants that it will not use or
 * redistribute the Software for such purposes.
 */
package examples;
import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;

import com.sun.j3d.utils.applet.MainFrame;
import com.sun.j3d.utils.geometry.ColorCube;
import com.sun.j3d.utils.universe.*;
import java.awt.AWTEvent;
import javax.media.j3d.*;

import java.awt.event.*;
import java.util.Enumeration;

//   SimpleBehaviorApp renders a single ColorCube
//   that rotates when any key is pressed.

public class SimpleBehaviorApp extends Applet {

    public class SimpleBehavior extends Behavior{
        private TransformGroup targetTG;
        private Transform3D rotation = new Transform3D();
        private double angle = 0.0;

        // create SimpleBehavior
        SimpleBehavior(TransformGroup targetTG){
            this.targetTG = targetTG;
        }

        // initialize the Behavior
        //     set initial wakeup condition
        //     called when behavior beacomes live
        public void initialize(){
            // set initial wakeup condition
//            this.wakeupOn(new WakeupOnAWTEvent(KeyEvent.KEY_PRESSED));
            this.wakeupOn(new WakeupOnAWTEvent(MouseEvent.MOUSE_WHEEL));

        }

        // behave
        // called by Java 3D when appropriate stimulus occures
        public void processStimulus(Enumeration criteria){
            // decode event
            AWTEvent myEvent;
            int i = 0;
            while (criteria.hasMoreElements()){
                WakeupOnAWTEvent event = (WakeupOnAWTEvent) criteria.nextElement();
                myEvent = event.getAWTEvent()[0];
                System.out.println("Element " + (i++) + " : " + event.getClass().getCanonicalName());
                System.out.println("Event ID = " + myEvent.getID());
                System.out.println("MouseWheelEvent.MOUSE_WHEEL_EVENT_MASK = " + MouseWheelEvent.MOUSE_WHEEL_EVENT_MASK);
                if (myEvent.getID() == MouseWheelEvent.MOUSE_WHEEL){
                    System.out.println("È stata attivata la rotellina");
                    System.out.println("STATO EVENTO : " + myEvent.paramString());
                    System.out.println("Rotazione rotellina : " + myEvent.paramString().substring(myEvent.paramString().lastIndexOf(',')));
                }
            }
            // do what is necessary
            angle += 0.1;
            rotation.rotY(angle);
            targetTG.setTransform(rotation);
//            this.wakeupOn(new WakeupOnAWTEvent(KeyEvent.KEY_PRESSED));
            this.wakeupOn(new WakeupOnAWTEvent(MouseEvent.MOUSE_WHEEL));
        }
    } // end of class SimpleBehavior

    public BranchGroup createSceneGraph() {
	// Create the root of the branch graph
	BranchGroup objRoot = new BranchGroup();
        TransformGroup objRotate = new TransformGroup();
        objRotate.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
	objRotate.addChild(new ColorCube(0.4));

        SimpleBehavior myRotationBehavior = new SimpleBehavior(objRotate);
        myRotationBehavior.setSchedulingBounds(new BoundingBox());
        objRoot.addChild(myRotationBehavior);

	// Let Java 3D perform optimizations on this scene graph.
        objRoot.compile();

	return objRoot;
    } // end of CreateSceneGraph method of SimpleBehaviorApp

    // Create a simple scene and attach it to the virtual universe

    public SimpleBehaviorApp() {
        setLayout(new BorderLayout());
        GraphicsConfiguration config =
           SimpleUniverse.getPreferredConfiguration();

        Canvas3D canvas3D = new Canvas3D(config);
        add("Center", canvas3D);

        BranchGroup scene = createSceneGraph();

        // SimpleUniverse is a Convenience Utility class
        SimpleUniverse simpleU = new SimpleUniverse(canvas3D);

	// This will move the ViewPlatform back a bit so the
	// objects in the scene can be viewed.
        simpleU.getViewingPlatform().setNominalViewingTransform();

        simpleU.addBranchGraph(scene);
    } // end of SimpleBehaviorApp (constructor)
    //  The following allows this to be run as an application
    //  as well as an applet

    public static void main(String[] args) {
        System.out.print("SimpleBehaviorApp.java \n- a demonstration of creating a simple");
        System.out.println("behavior class to provide interaction in a Java 3D scene.");
        System.out.println("When the app loads, press any key to make the cube rotate.");
        System.out.println("This is a simple example progam from The Java 3D API Tutorial.");
        System.out.println("The Java 3D Tutorial is available on the web at:");
        System.out.println("http://java.sun.com/products/java-media/3D/collateral");
        Frame frame = new MainFrame(new SimpleBehaviorApp(), 256, 256);

    } // end of main (method of SimpleBehaviorApp)

} // end of class SimpleBehaviorApp
