/*
 *      @(#)PickCallbackApp.java 1.1 00/09/22 16:24
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
import com.sun.j3d.utils.pickfast.behaviors.*;
import javax.media.j3d.*;
import javax.vecmath.*;

import java.awt.event.*;
import java.util.Enumeration;

//   PickCallbackApp renders two interactively rotatable cubes.

public class PickCallbackApp extends Applet {

    public class MyCallbackClass extends Object implements PickingCallback{
        public void transformChanged(int type, TransformGroup tg) {
                System.out.println("picking");
        }
    }

    public BranchGroup createSceneGraph(Canvas3D canvas) {
	// Create the root of the branch graph
	BranchGroup objRoot = new BranchGroup();

        TransformGroup objRotate = null;
        PickRotateBehavior pickRotate = null;
        Transform3D transform = new Transform3D();
        BoundingSphere behaveBounds = new BoundingSphere();

        // create ColorCube and PickRotateBehavior objects
        transform.setTranslation(new Vector3f(-0.6f, 0.0f, -0.6f));
        objRotate = new TransformGroup(transform);
        objRotate.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        objRotate.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        objRotate.setCapability(TransformGroup.ENABLE_PICK_REPORTING);

        objRoot.addChild(objRotate);
        objRotate.addChild(new ColorCube(0.4));

        pickRotate = new PickRotateBehavior(objRoot, canvas, behaveBounds);
        objRoot.addChild(pickRotate);

        PickingCallback myCallback = new MyCallbackClass();
        // Register the class callback to be called
        pickRotate.setupCallback(myCallback);

        // add a second ColorCube object to the scene graph
        transform.setTranslation(new Vector3f( 0.6f, 0.0f, -0.6f));
        objRotate = new TransformGroup(transform);
        objRotate.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        objRotate.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        objRotate.setCapability(TransformGroup.ENABLE_PICK_REPORTING);

        objRoot.addChild(objRotate);
        objRotate.addChild(new ColorCube(0.4));

	// Let Java 3D perform optimizations on this scene graph.
        objRoot.compile();

	return objRoot;
    } // end of CreateSceneGraph method of PickCallbackApp

    // Create a simple scene and attach it to the virtual universe

    public PickCallbackApp() {
        setLayout(new BorderLayout());
        GraphicsConfiguration config =
           SimpleUniverse.getPreferredConfiguration();

        Canvas3D canvas3D = new Canvas3D(config);
        add("Center", canvas3D);

        // SimpleUniverse is a Convenience Utility class
        SimpleUniverse simpleU = new SimpleUniverse(canvas3D);

        BranchGroup scene = createSceneGraph(canvas3D);

        // This will move the ViewPlatform back a bit
        simpleU.getViewingPlatform().setNominalViewingTransform();

        simpleU.addBranchGraph(scene);
    } // end of PickCallbackApp (constructor)

    //  The following allows this to be run as an application or as an applet

    public static void main(String[] args) {
        System.out.print("PickCallbackApp.java \n- a demonstration of using the PickRotateBehavior ");
        System.out.println("utility class to provide interaction in a Java 3D scene.");
        System.out.println("Hold the mouse button over a cube then move the mouse to make that cube rotate.");
        System.out.println("This is a simple example progam from The Java 3D API Tutorial.");
        System.out.println("The Java 3D Tutorial is available on the web at:");
        System.out.println("http://java.sun.com/products/java-media/3D/collateral");
        Frame frame = new MainFrame(new PickCallbackApp(), 256, 256);
    } // end of main (method of PickCallbackApp)

} // end of class PickCallbackApp
