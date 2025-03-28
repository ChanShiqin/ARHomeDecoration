package com.example.arhomedecorationapplication

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Color
import com.google.ar.sceneform.rendering.MaterialFactory
import com.google.ar.sceneform.rendering.ShapeFactory
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode

class DistanceMeasurementActivity : AppCompatActivity() {
//
//    private lateinit var arFragment: ArFragment
//    private lateinit var distanceTextView: TextView
//
//    private var firstHitPose: Pose? = null
//    private var secondHitPose: Pose? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_distance_measurement)

//        arFragment = supportFragmentManager.findFragmentById(R.id.arFragment) as ArFragment
//        distanceTextView = findViewById(R.id.distanceTextView)
//
//        // Set a listener to detect tap on AR plane
//        arFragment.setOnTapArPlaneListener { hitResult, plane, motionEvent ->
//            // Ensure the plane is horizontal and that we have tapped it.
//            if (plane.type != Plane.Type.HORIZONTAL_UPWARD_FACING) return@setOnTapArPlaneListener
//
//            // If this is the first tap, store the hit pose
//            if (firstHitPose == null) {
//                firstHitPose = hitResult.hitPose
//                addPointToScene(hitResult)
//            } else if (secondHitPose == null) {
//                secondHitPose = hitResult.hitPose
//                addPointToScene(hitResult)
//                calculateDistance()
//            }
//        }
    }

//    private fun addPointToScene(hitResult: HitResult) {
//        // Create a simple sphere to represent the point at the position
//        val sphere = ShapeFactory.makeSphere(0.05f, hitResult.hitPose)  // Directly use hitPose
//
//        // Create a material for the sphere
//        MaterialFactory.makeOpaqueWithColor(this, Color(1.0f, 0.0f, 0.0f))  // Red color
//            .thenAccept { material ->
//                sphere.material = material // Set the sphere's material to the red material
//            }
//
//        // Create a node and place it in the AR scene
//        val node = TransformableNode(arFragment.transformationSystem)
//        node.renderable = sphere
//        node.setParent(arFragment.arSceneView.scene)
//        node.worldPosition = hitResult.hitPose.translation  // Directly use translation as Vector3
//    }
//
//    private fun calculateDistance() {
//        if (firstHitPose == null || secondHitPose == null) {
//            Toast.makeText(this, "Please select two points on the plane", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        // Get the 3D positions of the tapped points (translation of hitPoses)
//        val firstPosition = firstHitPose?.translation
//        val secondPosition = secondHitPose?.translation
//
//        if (firstPosition != null && secondPosition != null) {
//            val distance = calculateEuclideanDistance(firstPosition, secondPosition)
//            distanceTextView.text = String.format("Distance: %.2f meters", distance)
//        }
//    }
//
//    private fun calculateEuclideanDistance(point1: FloatArray, point2: FloatArray): Float {
//        // Calculate Euclidean distance between two points in 3D space
//        val dx = point2[0] - point1[0]
//        val dy = point2[1] - point1[1]
//        val dz = point2[2] - point1[2]
//
//        return Math.sqrt((dx * dx + dy * dy + dz * dz).toDouble()).toFloat()
//    }
}
