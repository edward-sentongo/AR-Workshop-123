package com.sentongoapps.sogetiaranimal;

import android.animation.ObjectAnimator;
import android.support.annotation.Nullable;
import android.view.animation.LinearInterpolator;

import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.QuaternionEvaluator;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.math.Vector3Evaluator;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.Material;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.ar.sceneform.ux.TransformationSystem;

public class AnimalNode extends TransformableNode {

    public enum MotionDirection {
        LEFT,
        RIGHT,
        FORWARD,
        BACKWARD;

        public static MotionDirection fromInteger(int intValue) {
            switch(intValue) {
                case 0:
                    return LEFT;
                case 1:
                    return RIGHT;
                case 2:
                    return FORWARD;
                default:
                    return BACKWARD;
            }
        }
    }

    public enum MotionAnimation {
        ROTATION,
        LINEAR,
        KILL;

        public static MotionAnimation fromInteger(int intValue) {
            switch(intValue) {
                case 0:
                    return ROTATION;
                case 1:
                    return LINEAR;
                default:
                    return KILL;
            }
        }
    }

    // We'll use Property Animation to make this node rotate.
    @Nullable
    private ObjectAnimator rotationAnimation = null;

    @Nullable
    private ObjectAnimator walkingAnimation = null;

    private boolean isRotating = false;
    private boolean isWalking = false;
    private boolean isDead = false;

    public AnimalNode(TransformationSystem transformationSystem) {
        super(transformationSystem);
    }

    @Override
    public void onUpdate(FrameTime frameTime) {
        super.onUpdate(frameTime);

        if (rotationAnimation != null) {
            rotationAnimation.resume();
            float animatedFraction = rotationAnimation.getAnimatedFraction();
            rotationAnimation.setDuration(getAnimationDuration());
            rotationAnimation.setCurrentFraction(animatedFraction);
        } else if (walkingAnimation != null) {
            walkingAnimation.resume();
            float animatedFraction = walkingAnimation.getAnimatedFraction();
            walkingAnimation.setDuration(getAnimationDuration());
            walkingAnimation.setCurrentFraction(animatedFraction);
        }
    }

    @Override
    public void onActivate() {
    }

    @Override
    public void onDeactivate() {
        stopRotationAnimation();
        stopWalkingAnimation();
    }

    private long getAnimationDuration() {
        return (long) (1000 * 360 / (47f * 2));
    }

    public void toggleRotation(){
        if (isRotating) {
            stopRotationAnimation();
        } else {
            startRotationAnimation();
        }
        isRotating = !isRotating;
    }

    public void toggleWalking(MotionDirection motionDirection) {
        if (isWalking){
            stopWalkingAnimation();
        } else {
            startWalkingAnimation(motionDirection);
        }
        isWalking = !isWalking;
    }

    public void toggleLife(){
        if (isDead) {
            setAlive();
        } else {
            setDead();
        }
        isDead = !isDead;
    }

    private void startRotationAnimation() {
        if (rotationAnimation != null) {
            return;
        }
        rotationAnimation = createRotationAnimator();
        rotationAnimation.setTarget(this);
        rotationAnimation.setDuration(getAnimationDuration());
        rotationAnimation.start();
    }

    private void stopRotationAnimation() {
        if (rotationAnimation== null) {
            return;
        }
        rotationAnimation.cancel();
        rotationAnimation = null;
    }

    private void startWalkingAnimation(MotionDirection motionDirection) {
        if (walkingAnimation != null) {
            return;
        }
        walkingAnimation = createWalkingAnimator(this, motionDirection);
        walkingAnimation.setTarget(this);
        walkingAnimation.setDuration(getAnimationDuration());
        walkingAnimation.start();
    }

    private void stopWalkingAnimation() {
        if (walkingAnimation == null) {
            return;
        }
        walkingAnimation.cancel();
        walkingAnimation = null;
    }

    private void setAlive() {
        setLocalRotation(Quaternion.axisAngle(new Vector3(0.0f, 0.0f, 0.0f), 0));
        Material readableMaterial = getRenderable().getMaterial().makeCopy();
        readableMaterial.setFloat3("baseColorTint", new Color(1.0f, 1.0f, 1.0f, 1.0f));
        getRenderable().setMaterial(readableMaterial);
    }

    private void setDead() {
        setLocalRotation(Quaternion.axisAngle(new Vector3(0.0f, 0.0f, 1.0f), 90));

        Material readableMaterial = getRenderable().getMaterial().makeCopy();
        readableMaterial.setFloat3("baseColorTint", new Color(1.0f, 0.0f, 0.0f, 0.6f));
        getRenderable().setMaterial(readableMaterial);
    }

    /** Returns an ObjectAnimator that makes this node rotate. */
    private static ObjectAnimator createRotationAnimator() {
        // Node's setLocalRotation method accepts Quaternions as parameters.
        // First, set up orientations that will animate a circle.
        Quaternion orientation1 = Quaternion.axisAngle(new Vector3(0.0f, 1.0f, 0.0f), 0);
        Quaternion orientation2 = Quaternion.axisAngle(new Vector3(0.0f, 1.0f, 0.0f), 120);
        Quaternion orientation3 = Quaternion.axisAngle(new Vector3(0.0f, 1.0f, 0.0f), 240);
        Quaternion orientation4 = Quaternion.axisAngle(new Vector3(0.0f, 1.0f, 0.0f), 360);

        ObjectAnimator orbitAnimation = new ObjectAnimator();
        orbitAnimation.setObjectValues(orientation1, orientation2, orientation3, orientation4);

        // Next, give it the localRotation property.
        orbitAnimation.setPropertyName("localRotation");

        // Use Sceneform's QuaternionEvaluator.
        orbitAnimation.setEvaluator(new QuaternionEvaluator());

        //  Allow orbitAnimation to repeat forever
        orbitAnimation.setRepeatCount(ObjectAnimator.INFINITE);
        orbitAnimation.setRepeatMode(ObjectAnimator.RESTART);
        orbitAnimation.setInterpolator(new LinearInterpolator());
        orbitAnimation.setAutoCancel(true);

        return orbitAnimation;
    }

    /** Returns an ObjectAnimator that makes this node move forward. */
    private static ObjectAnimator createWalkingAnimator(AnimalNode nodeToWalk, MotionDirection motionDirection) {
        /* Notes:
        Vector3
        x: makes it move sideways.
        y: makes it ascend a.k.a climb upwards
        z: makes it move "forward" to wards the phone

        thus,
         - if facing away from phone, move -Z, if facing phone, move +z
         - if facing left, move -X, if facing right, move +X
         - else up & down for choice you may wish
         */
        Vector3 location1 = new Vector3();
        Vector3 location2= new Vector3();
        Vector3 location3= new Vector3();
        Vector3 location4= new Vector3();

        switch (motionDirection){
            case LEFT:
                location1 = new Vector3(nodeToWalk.getLocalPosition().x - 0.1f, 0.0f, 0.0f);
                location2 = new Vector3(nodeToWalk.getLocalPosition().x - 0.2f, 0.0f, 0.0f);
                location3 = new Vector3(nodeToWalk.getLocalPosition().x - 0.3f, 0.0f, 0.0f);
                location4 = new Vector3(nodeToWalk.getLocalPosition().x - 0.4f, 0.0f, 0.0f);
                break;
            case RIGHT:
                location1 = new Vector3(nodeToWalk.getLocalPosition().x + 0.1f, 0.0f, 0.0f);
                location2 = new Vector3(nodeToWalk.getLocalPosition().x + 0.2f, 0.0f, 0.0f);
                location3 = new Vector3(nodeToWalk.getLocalPosition().x + 0.3f, 0.0f, 0.0f);
                location4 = new Vector3(nodeToWalk.getLocalPosition().x + 0.4f, 0.0f, 0.0f);
                break;
            case FORWARD:
                location1 = new Vector3(0.0f, 0.0f, nodeToWalk.getLocalPosition().x - 0.1f);
                location2 = new Vector3(0.0f, 0.0f, nodeToWalk.getLocalPosition().x - 0.2f);
                location3 = new Vector3(0.0f, 0.0f, nodeToWalk.getLocalPosition().x - 0.3f);
                location4 = new Vector3(0.0f, 0.0f, nodeToWalk.getLocalPosition().x - 0.4f);
                break;
            case BACKWARD:
                location1 = new Vector3(0.0f, 0.0f, nodeToWalk.getLocalPosition().x + 0.1f);
                location2 = new Vector3(0.0f, 0.0f, nodeToWalk.getLocalPosition().x + 0.2f);
                location3 = new Vector3(0.0f, 0.0f, nodeToWalk.getLocalPosition().x + 0.3f);
                location4 = new Vector3(0.0f, 0.0f, nodeToWalk.getLocalPosition().x + 0.4f);
                break;

        }

        ObjectAnimator linearAnimation = new ObjectAnimator();
        linearAnimation.setObjectValues(location1, location2, location3, location4);

        // Next, give it the localRotation property.
        linearAnimation.setPropertyName("localPosition");

        // Use Sceneform's QuaternionEvaluator.
        linearAnimation.setEvaluator(new Vector3Evaluator());

        //  Allow orbitAnimation to repeat forever
        linearAnimation.setInterpolator(new LinearInterpolator());
        linearAnimation.setAutoCancel(true);

        return linearAnimation;
    }
}
