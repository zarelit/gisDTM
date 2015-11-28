/*
 *      @(#)Clock.java 1.0 1.1 00/09/22 14:37
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
import javax.media.j3d.*;
import javax.vecmath.*;

import java.util.Calendar;


//   ClockApp renders an animated analog clock

    public class Clock extends BranchGroup {
        private Shape3D cFace = new Shape3D();
        private Shape3D fhHand = new Shape3D();
        private Shape3D fmHand = new Shape3D();
        private Shape3D bhHand = new Shape3D();
        private Shape3D bmHand = new Shape3D();
        private TransformGroup fhHandPos = new TransformGroup();
        private TransformGroup fmHandPos = new TransformGroup();
        private TransformGroup bhHandPos = new TransformGroup();
        private TransformGroup bmHandPos = new TransformGroup();

        public Clock () {
            double hourOffset, minOffset;
            BoundingSphere bounds = new BoundingSphere();

            // create Alpha object that loop continuously with 12 hour period
            Alpha timer = new Alpha (-1, 12*60*60*1000);

            Calendar cal = Calendar.getInstance();
            minOffset  = 2.0 * Math.PI * (double) cal.get(Calendar.MINUTE) / 60.0;
            hourOffset = 2.0 * Math.PI * (double) cal.get(Calendar.HOUR) / 12.0 + minOffset/12.0;

            // create Interpolators
            AxisAngle4f faxisOfRot = new AxisAngle4f(1.0f,0.0f,0.0f,(float)Math.PI/-2.0f);
            Transform3D faxisT3D = new Transform3D();
            faxisT3D.set(faxisOfRot);

            RotationInterpolator fhHandInterp = new RotationInterpolator(timer,fhHandPos);
            fhHandInterp.setMinimumAngle((float) hourOffset);
            fhHandInterp.setMaximumAngle((float) (hourOffset + Math.PI * 2.0));
            fhHandInterp.setSchedulingBounds(bounds);
            fhHandInterp.setTransformAxis(faxisT3D);

            RotationInterpolator fmHandInterp = new RotationInterpolator(timer,fmHandPos);
            fmHandInterp.setMinimumAngle((float) minOffset);
            fmHandInterp.setMaximumAngle((float) (minOffset + Math.PI * 24.0));
            fmHandInterp.setSchedulingBounds(bounds);
            fmHandInterp.setTransformAxis(faxisT3D);

            AxisAngle4f baxisOfRot = new AxisAngle4f(1.0f,0.0f,0.0f,(float)Math.PI/2.0f);
            Transform3D baxisT3D = new Transform3D();
            baxisT3D.set(baxisOfRot);

            RotationInterpolator bhHandInterp = new RotationInterpolator(timer,bhHandPos);
            bhHandInterp.setMinimumAngle((float) hourOffset);
            bhHandInterp.setMaximumAngle((float) (hourOffset + Math.PI * 2.0));
            bhHandInterp.setSchedulingBounds(bounds);
            bhHandInterp.setTransformAxis(baxisT3D);

            RotationInterpolator bmHandInterp = new RotationInterpolator(timer,bmHandPos);
            bmHandInterp.setMinimumAngle((float) minOffset);
            bmHandInterp.setMaximumAngle((float) (minOffset + Math.PI * 24.0));
            bmHandInterp.setSchedulingBounds(bounds);
            bmHandInterp.setTransformAxis(baxisT3D);

            // assemble scene graph
            this.addChild(cFace);
            this.addChild(fhHandPos);
            this.addChild(fmHandPos);
            this.addChild(fhHandInterp);
            this.addChild(fmHandInterp);
            fhHandPos.addChild(fhHand);
            fmHandPos.addChild(fmHand);

            this.addChild(bhHandPos);
            this.addChild(bmHandPos);
            this.addChild(bhHandInterp);
            this.addChild(bmHandInterp);
            bhHandPos.addChild(bhHand);
            bmHandPos.addChild(bmHand);

            // set Capabilities for TransformGroups
            fhHandPos.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
            fmHandPos.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

            bhHandPos.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
            bmHandPos.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

            // add geometry and appearance to Shape3D nodes
            cFace.setGeometry(createFaceGeometry());
            fhHand.setGeometry(createHourHandGeometry());
            fmHand.setGeometry(createMinuteHandGeometry());
            bhHand.setGeometry(createHourHandGeometry());
            bmHand.setGeometry(createMinuteHandGeometry());
            setAppearance(createDefaultAppearance());

        }

        Geometry createFaceGeometry() {
            QuadArray geom = new QuadArray(16, GeometryArray.COORDINATES);
            geom.setCoordinate( 0, new Point3f(-0.05f, 0.90f, 0.0f));
            geom.setCoordinate( 1, new Point3f( 0.05f, 0.90f, 0.0f));
            geom.setCoordinate( 2, new Point3f( 0.05f, 1.00f, 0.0f));
            geom.setCoordinate( 3, new Point3f(-0.05f, 1.00f, 0.0f));
            geom.setCoordinate( 4, new Point3f( 0.90f,-0.05f, 0.0f));
            geom.setCoordinate( 5, new Point3f( 1.00f,-0.05f, 0.0f));
            geom.setCoordinate( 6, new Point3f( 1.00f, 0.05f, 0.0f));
            geom.setCoordinate( 7, new Point3f( 0.90f, 0.05f, 0.0f));
            geom.setCoordinate( 8, new Point3f(-0.05f,-1.00f, 0.0f));
            geom.setCoordinate( 9, new Point3f( 0.05f,-1.00f, 0.0f));
            geom.setCoordinate(10, new Point3f( 0.05f,-0.90f, 0.0f));
            geom.setCoordinate(11, new Point3f(-0.05f,-0.90f, 0.0f));
            geom.setCoordinate(12, new Point3f(-1.00f,-0.05f, 0.0f));
            geom.setCoordinate(13, new Point3f(-0.90f,-0.05f, 0.0f));
            geom.setCoordinate(14, new Point3f(-0.90f, 0.05f, 0.0f));
            geom.setCoordinate(15, new Point3f(-1.00f, 0.05f, 0.0f));

            return geom;
        }

        Geometry createHourHandGeometry() {
            QuadArray geom = new QuadArray(4, GeometryArray.COORDINATES);
            geom.setCoordinate(0, new Point3f(-0.05f, 0.0f, 0.0f));
            geom.setCoordinate(1, new Point3f( 0.05f, 0.0f, 0.0f));
            geom.setCoordinate(2, new Point3f( 0.05f, 0.5f, 0.0f));
            geom.setCoordinate(3, new Point3f(-0.05f, 0.5f, 0.0f));

            return geom;
        }

        Geometry createMinuteHandGeometry() {
            QuadArray geom = new QuadArray(4, GeometryArray.COORDINATES);
            geom.setCoordinate(0, new Point3f(-0.02f, 0.0f, 0.0001f));
            geom.setCoordinate(1, new Point3f( 0.02f, 0.0f, 0.0001f));
            geom.setCoordinate(2, new Point3f( 0.02f, 1.0f, 0.0001f));
            geom.setCoordinate(3, new Point3f(-0.02f, 1.0f, 0.0001f));

            return geom;
        }

        Appearance createDefaultAppearance() {
            Appearance appear = new Appearance();

            // set color to blue
            ColoringAttributes colorAttrib = new ColoringAttributes();
            colorAttrib.setColor(new Color3f(0.0f, 0.0f, 1.0f));
            appear.setColoringAttributes(colorAttrib);

            return appear;
        }

        public void setAppearance(Appearance appear) {
            fmHand.setAppearance(appear);
            fhHand.setAppearance(appear);

            // set culling to FRONT (show the back side only for back hands)
            Appearance bAppear = (Appearance) appear.cloneNodeComponent(false);
            PolygonAttributes polyAttrib = new PolygonAttributes();
            polyAttrib.setCullFace(PolygonAttributes.CULL_FRONT);
            bAppear.setPolygonAttributes(polyAttrib);
            bmHand.setAppearance(bAppear);
            bhHand.setAppearance(bAppear);

            // set culling to NONE (show both sides for the face)
            Appearance fAppear = (Appearance) appear.cloneNodeComponent(false);
            PolygonAttributes fPolyAttrib = new PolygonAttributes();
            fPolyAttrib.setCullFace(PolygonAttributes.CULL_NONE);
            fAppear.setPolygonAttributes(fPolyAttrib);
            cFace.setAppearance(fAppear);
        }

    }  // end of class Clock

