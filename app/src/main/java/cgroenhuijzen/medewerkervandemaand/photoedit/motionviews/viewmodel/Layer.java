package cgroenhuijzen.medewerkervandemaand.photoedit.motionviews.viewmodel;

import androidx.annotation.FloatRange;

/**
 * Medewerker van de maand app
 * Caren Groenhuijzen
 * NOVI Hogeschool - SD-Praktijk 1
 * 14-08-2020
 * <p>
 * Dit bestand is overgenomen van AndriyBas.
 * Op GitHub: <a href = https://github.com/uptechteam/MotionViews-Android>MotionViews-Android</a>
 */

public class Layer {

    /**
     * rotation relative to the layer center, in degrees
     */
    @FloatRange(from = 0.0F, to = 360.0F)
    private float rotationInDegrees;

    private float scale;
    /**
     * top left X coordinate, relative to parent canvas
     */
    private float x;
    /**
     * top left Y coordinate, relative to parent canvas
     */
    private float y;
    /**
     * is layer flipped horizontally (by X-coordinate)
     */
    private boolean isFlipped;

    public Layer() {
        reset();
    }

    protected void reset() {
        this.rotationInDegrees = 0.0F;
        this.scale = 1.0F;
        this.isFlipped = false;
        this.x = 0.0F;
        this.y = 0.0F;
    }

    public void postScale(float scaleDiff) {
        float newVal = scale + scaleDiff;
        if (newVal >= getMinScale() && newVal <= getMaxScale()) {
            scale = newVal;
        }
    }

    protected float getMaxScale() {
        return Limits.MAX_SCALE;
    }

    protected float getMinScale() {
        return Limits.MIN_SCALE;
    }

    public void postRotate(float rotationInDegreesDiff) {
        this.rotationInDegrees += rotationInDegreesDiff;
        this.rotationInDegrees %= 360.0F;
    }

    public void postTranslate(float dx, float dy) {
        this.x += dx;
        this.y += dy;
    }

    public void flip() {
        this.isFlipped = !isFlipped;
    }

    public float initialScale() {
        return Limits.INITIAL_ENTITY_SCALE;
    }

    public float getRotationInDegrees() {
        return rotationInDegrees;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public boolean isFlipped() {
        return isFlipped;
    }

    interface Limits {
        float MIN_SCALE = 0.06F;
        float MAX_SCALE = 4.0F;
        float INITIAL_ENTITY_SCALE = 0.4F;
    }
}
