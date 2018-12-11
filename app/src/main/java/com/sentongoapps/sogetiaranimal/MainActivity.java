package com.sentongoapps.sogetiaranimal;

import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Trackable;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.ux.ArFragment;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MainActivity extends AppCompatActivity {

    private ArFragment fragment;
    private PreferenceService preferenceService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragment = (ArFragment)
                getSupportFragmentManager().findFragmentById(R.id.sceneform_fragment);

        fragment.getArSceneView().getScene().addOnUpdateListener(frameTime -> {
            fragment.onUpdate(frameTime);
        });

        initializeGallery();

        preferenceService = new PreferenceService(this);

        Button menuButton = findViewById(R.id.menu_btn);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PreferenceMenu preferenceMenu = new PreferenceMenu(MainActivity.this);
                preferenceMenu.show();
            }
        });
    }

    private android.graphics.Point getScreenCenter() {
        View vw = findViewById(android.R.id.content);
        return new android.graphics.Point(vw.getWidth()/2, vw.getHeight()/2);
    }

    private void initializeGallery() {
        LinearLayout gallery = findViewById(R.id.gallery_layout);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1.0f
        );

        ImageView foxImageView = new ImageView(this);
        foxImageView.setImageResource(R.drawable.fox_image);
        foxImageView.setContentDescription("Fox");
        foxImageView.setOnClickListener(view ->{addObject(Uri.parse("Fox.sfb"));});
        foxImageView.setLayoutParams(params);
        gallery.addView(foxImageView);

        ImageView lionImageView = new ImageView(this);
        lionImageView.setImageResource(R.drawable.lion_image);
        lionImageView.setContentDescription("lion");
        lionImageView.setOnClickListener(view ->{addObject(Uri.parse("Lion.sfb"));});
        lionImageView.setLayoutParams(params);
        gallery.addView(lionImageView);

        ImageView zebraImageView = new ImageView(this);
        zebraImageView.setImageResource(R.drawable.zebra_image);
        zebraImageView.setContentDescription("zebra");
        zebraImageView.setOnClickListener(view ->{addObject(Uri.parse("Zebra.sfb"));});
        zebraImageView.setLayoutParams(params);
        gallery.addView(zebraImageView);
    }

    private void addObject(Uri model) {
        Frame frame = fragment.getArSceneView().getArFrame();
        android.graphics.Point pt = getScreenCenter();
        List<HitResult> hits;
        if (frame != null) {
            hits = frame.hitTest(pt.x, pt.y);
            for (HitResult hit : hits) {
                Trackable trackable = hit.getTrackable();
                if (trackable instanceof Plane &&
                        ((Plane) trackable).isPoseInPolygon(hit.getHitPose())) {
                    placeObject(fragment, hit.createAnchor(), model);
                    break;

                }
            }
        }
    }

    private void placeObject(ArFragment fragment, Anchor anchor, Uri model) {
        CompletableFuture<Void> renderableFuture =
                ModelRenderable.builder()
                        .setSource(fragment.getContext(), model)
                        .build()
                        .thenAccept(renderable -> addNodeToScene(fragment, anchor, renderable))
                        .exceptionally((throwable -> {
                            AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            builder.setMessage(throwable.getMessage())
                                    .setTitle("Sogeti AR Error!");
                            AlertDialog dialog = builder.create();
                            dialog.show();
                            return null;
                        }));
    }

    private void addNodeToScene(ArFragment fragment, Anchor anchor, Renderable renderable) {
        AnchorNode anchorNode = new AnchorNode(anchor);
        AnimalNode node = new AnimalNode(fragment.getTransformationSystem());
        node.setRenderable(renderable);
        node.setParent(anchorNode);
        node.setOnTapListener(new Node.OnTapListener() {
            @Override
            public void onTap(HitTestResult hitTestResult, MotionEvent motionEvent) {
                AnimalNode.MotionAnimation motionAnimation = preferenceService.getMotionAnimation();

                switch (motionAnimation){
                    case LINEAR:
                        node.toggleWalking(preferenceService.getMotionDirection());
                        break;
                    case ROTATION:
                        node.toggleRotation();
                        break;
                    case KILL:
                        node.toggleLife();
                        break;
                }
            }
        });
        fragment.getArSceneView().getScene().addChild(anchorNode);
        node.select();
    }

}
