package episode_24;

import episode_25.Face;
import episode_25.OBJLoader;
import episode_25.Model;
import episode_19.*;
import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.opengl.GLContext;
import static org.lwjgl.util.glu.GLU.gluPerspective;
import org.lwjgl.util.vector.Vector3f;
import org.newdawn.slick.Color;

/**
* Loads the Stanford bunny .obj file and draws it.
* @author Oskar Veerhoek
 */
public class ModelDemo {

    /**
     * Defines if the application is resizable.
     */
    public static final boolean resizable = true;
    /*
     * Defines if the application is running. Set to false to terminate the program.
     */
    public static volatile boolean running = true;
    /**
     * The position of the player as a 3D vector (xyz).
     */
    public static Vector3f position = new Vector3f(0, 0, 0);
    /**
     * The rotation of the axis (where to the player looks). The X component stands for the rotation along the x-axis, where 0 is dead ahead, 180 is
     * backwards, and 360 is automically set to 0 (dead ahead). The value must be between (including) 0 and 360. The Y component stands for the
     * rotation along the y-axis, where 0 is looking straight ahead, -90 is straight up, and 90 is straight down. The value must be between
     * (including) -90 and 90.
     */
    public static Vector3f rotation = new Vector3f(0, 0, 0);
    /**
     * The minimal distance from the camera where objects are rendered.
     */
    public static float zNear = 0.3f;
    /**
     * The width and length of the floor and ceiling. Don't put anything above 1000, or OpenGL will start to freak out, though.
     */
    public static final int gridSize = 10;
    /**
     * The size of tiles, where 0.5 is the standard size. Increasing the size by results in smaller tiles, and vice versa.
     */
    public static final float tileSize = 0.20f;
    /**
     * The maximal distance from the camera where objects are rendered.
     */
    public static float zFar = 100f;
    /**
     * The distance where fog starts appearing.
     */
    public static float fogNear = 19f;
    /**
     * The distance where the fog stops appearing (fully black here)
     */
    public static float fogFar = 26f;
    /**
     * The color of the fog in rgba.
     */
    public static Color fogColor = new Color(0f, 0f, 0f, 1f);
    /**
     * Defines if the application utilizes full-screen.
     */
    public static final boolean fullscreen = false;
    /**
     * Defines the walking speed, where 10 is the standard.
     */
    public static int walkingSpeed = 10;
    /**
     * Defines the mouse speed.
     */
    public static int mouseSpeed = 2;
    /**
     * Defines if the application utilizes vertical synchronization (eliminates screen tearing; caps fps to 60fps)
     */
    public static final boolean vsync = true;
    /**
     * Defines if the applications prints its frames-per-second to the console.
     */
    public static boolean printFPS = false;
    /**
     * Defines the maximum angle at which the player can look up.
     */
    public static final int maxLookUp = 85;
    /**
     * Defines the minimum angle at which the player can look down.
     */
    public static final int maxLookDown = -85;
    /**
     * The height of the ceiling.
     */
    public static final float ceilingHeight = 10;
    /**
     * The height of the floor.
     */
    public static final float floorHeight = -1;
    /**
     * Defines the field of view.
     */
    public static int fov = 68;
    private static int fps;
    private static long lastFPS;
    private static long lastFrame;

    private static long getTime() {
        return (Sys.getTime() * 1000) / Sys.getTimerResolution();
    }

    private static int getDelta() {
        long currentTime = getTime();
        int delta = (int) (currentTime - lastFrame);
        lastFrame = getTime();
        return delta;
    }

    public static void updateFPS() {
        if (getTime() - lastFPS > 1000) {
            if (printFPS) {
                System.out.println("FPS: " + fps);
            }
            fps = 0;
            lastFPS += 1000;
        }
        fps++;
    }

    public static void main(String[] args) {
        try {
            if (fullscreen) {
                Display.setDisplayModeAndFullscreen(Display.getDesktopDisplayMode());
            } else {
                Display.setResizable(resizable);
                Display.setDisplayMode(new DisplayMode(800, 600));
            }
            Display.setTitle("Lighting Test");
            Display.setVSyncEnabled(vsync);
            Display.create();
        } catch (LWJGLException ex) {
            System.err.println("Display initialization failed.");
            System.exit(1);
        }

        if (fullscreen) {
            Mouse.setGrabbed(true);
        } else {
            Mouse.setGrabbed(false);
        }

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        gluPerspective(fov, (float) Display.getWidth() / (float) Display.getHeight(), zNear, zFar);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        glEnable(GL_DEPTH_TEST);

        int objectDisplayList = glGenLists(1);
        glNewList(objectDisplayList, GL_COMPILE);
        {
            Model m = null;
            try {
                m = OBJLoader.loadModel(new File("src/episode_24/bunny.obj"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Display.destroy();
                System.exit(1);
            } catch (IOException e) {
                e.printStackTrace();
                Display.destroy();
                System.exit(1);
            }
            glBegin(GL_TRIANGLES);
            for (Face face : m.faces) {
                Vector3f n1 = m.normals.get((int) face.normal.x - 1);
                glNormal3f(n1.x, n1.y, n1.z);
                Vector3f v1 = m.vertices.get((int) face.vertex.x - 1);
                glVertex3f(v1.x, v1.y, v1.z);
                Vector3f n2 = m.normals.get((int) face.normal.y - 1);
                glNormal3f(n2.x, n2.y, n2.z);
                Vector3f v2 = m.vertices.get((int) face.vertex.y - 1);
                glVertex3f(v2.x, v2.y, v2.z);
                Vector3f n3 = m.normals.get((int) face.normal.z - 1);
                glNormal3f(n3.x, n3.y, n3.z);
                Vector3f v3 = m.vertices.get((int) face.vertex.z - 1);
                glVertex3f(v3.x, v3.y, v3.z);
            }
            glEnd();
        }
        glEndList();

        getDelta();
        lastFPS = getTime();

        while (running) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

            int delta = getDelta();

            glCallList(objectDisplayList);

            glLoadIdentity();
            glRotatef(rotation.x, 1, 0, 0);
            glRotatef(rotation.y, 0, 1, 0);
            glRotatef(rotation.z, 0, 0, 1);
            glTranslatef(position.x, position.y, position.z);
            

            // <editor-fold defaultstate="collapsed" desc="standard code">

            if (Mouse.isGrabbed()) {
                float mouseDX = Mouse.getDX() * mouseSpeed * 0.16f;
                float mouseDY = Mouse.getDY() * mouseSpeed * 0.16f;
                if (rotation.y + mouseDX >= 360) {
                    rotation.y = rotation.y + mouseDX - 360;
                } else if (rotation.y + mouseDX < 0) {
                    rotation.y = 360 - rotation.y + mouseDX;
                } else {
                    rotation.y += mouseDX;
                }
                if (rotation.x - mouseDY >= maxLookDown && rotation.x - mouseDY <= maxLookUp) {
                    rotation.x += -mouseDY;
                } else if (rotation.x - mouseDY < maxLookDown) {
                    rotation.x = maxLookDown;
                } else if (rotation.x - mouseDY > maxLookUp) {
                    rotation.x = maxLookUp;
                }
            }

            boolean keyUp = Keyboard.isKeyDown(Keyboard.KEY_UP) || Keyboard.isKeyDown(Keyboard.KEY_W);
            boolean keyDown = Keyboard.isKeyDown(Keyboard.KEY_DOWN) || Keyboard.isKeyDown(Keyboard.KEY_S);
            boolean keyLeft = Keyboard.isKeyDown(Keyboard.KEY_LEFT) || Keyboard.isKeyDown(Keyboard.KEY_A);
            boolean keyRight = Keyboard.isKeyDown(Keyboard.KEY_RIGHT) || Keyboard.isKeyDown(Keyboard.KEY_D);
            boolean flyUp = Keyboard.isKeyDown(Keyboard.KEY_SPACE);
            boolean flyDown = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);
            boolean moveFaster = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL);
            boolean moveSlower = Keyboard.isKeyDown(Keyboard.KEY_TAB);

            if (moveFaster && !moveSlower) {
                walkingSpeed *= 8f;
            }
            if (moveSlower && !moveFaster) {
                walkingSpeed /= 10f;
            }

            if (keyUp && keyRight && !keyLeft && !keyDown) {
                float angle = rotation.y + 45;
                Vector3f newPosition = new Vector3f(position);
                float schuine = (walkingSpeed * 0.0002f) * delta;
                float aanliggende = schuine * (float) Math.cos(Math.toRadians(angle));
                float overstaande = (float) (Math.sin(Math.toRadians(angle)) * schuine);
                newPosition.z += aanliggende;
                newPosition.x -= overstaande;
                position.z = newPosition.z;
                position.x = newPosition.x;
            }
            if (keyUp && keyLeft && !keyRight && !keyDown) {
                float angle = rotation.y - 45;
                Vector3f newPosition = new Vector3f(position);
                float schuine = (walkingSpeed * 0.0002f) * delta;
                float aanliggende = schuine * (float) Math.cos(Math.toRadians(angle));
                float overstaande = (float) (Math.sin(Math.toRadians(angle)) * schuine);
                newPosition.z += aanliggende;
                newPosition.x -= overstaande;
                position.z = newPosition.z;
                position.x = newPosition.x;
            }
            if (keyUp && !keyLeft && !keyRight && !keyDown) {
                float angle = rotation.y;
                Vector3f newPosition = new Vector3f(position);
                float schuine = (walkingSpeed * 0.0002f) * delta;
                float aanliggende = schuine * (float) Math.cos(Math.toRadians(angle));
                float overstaande = (float) (Math.sin(Math.toRadians(angle)) * schuine);
                newPosition.z += aanliggende;
                newPosition.x -= overstaande;
                position.z = newPosition.z;
                position.x = newPosition.x;
            }
            if (keyDown && keyLeft && !keyRight && !keyUp) {
                float angle = rotation.y - 135;
                Vector3f newPosition = new Vector3f(position);
                float schuine = (walkingSpeed * 0.0002f) * delta;
                float aanliggende = schuine * (float) Math.cos(Math.toRadians(angle));
                float overstaande = (float) (Math.sin(Math.toRadians(angle)) * schuine);
                newPosition.z += aanliggende;
                newPosition.x -= overstaande;
                position.z = newPosition.z;
                position.x = newPosition.x;
            }
            if (keyDown && keyRight && !keyLeft && !keyUp) {
                float angle = rotation.y + 135;
                Vector3f newPosition = new Vector3f(position);
                float schuine = (walkingSpeed * 0.0002f) * delta;
                float aanliggende = schuine * (float) Math.cos(Math.toRadians(angle));
                float overstaande = (float) (Math.sin(Math.toRadians(angle)) * schuine);
                newPosition.z += aanliggende;
                newPosition.x -= overstaande;
                position.z = newPosition.z;
                position.x = newPosition.x;
            }
            if (keyDown && !keyUp && !keyLeft && !keyRight) {
                float angle = rotation.y;
                Vector3f newPosition = new Vector3f(position);
                float schuine = -(walkingSpeed * 0.0002f) * delta;
                float aanliggende = schuine * (float) Math.cos(Math.toRadians(angle));
                float overstaande = (float) (Math.sin(Math.toRadians(angle)) * schuine);
                newPosition.z += aanliggende;
                newPosition.x -= overstaande;
                position.z = newPosition.z;
                position.x = newPosition.x;
            }
            if (keyLeft && !keyRight && !keyUp && !keyDown) {
                float angle = rotation.y - 90;
                Vector3f newPosition = new Vector3f(position);
                float schuine = (walkingSpeed * 0.0002f) * delta;
                float aanliggende = schuine * (float) Math.cos(Math.toRadians(angle));
                float overstaande = (float) (Math.sin(Math.toRadians(angle)) * schuine);
                newPosition.z += aanliggende;
                newPosition.x -= overstaande;
                position.z = newPosition.z;
                position.x = newPosition.x;
            }
            if (keyRight && !keyLeft && !keyUp && !keyDown) {
                float angle = rotation.y + 90;
                Vector3f newPosition = new Vector3f(position);
                float schuine = (walkingSpeed * 0.0002f) * delta;
                float aanliggende = schuine * (float) Math.cos(Math.toRadians(angle));
                float overstaande = (float) (Math.sin(Math.toRadians(angle)) * schuine);
                newPosition.z += aanliggende;
                newPosition.x -= overstaande;
                position.z = newPosition.z;
                position.x = newPosition.x;
            }
            if (flyUp && !flyDown) {
                double newPositionY = (walkingSpeed * 0.0002) * delta;
                position.y -= newPositionY;
            }
            if (flyDown && !flyUp) {
                double newPositionY = (walkingSpeed * 0.0002) * delta;
                position.y += newPositionY;
            }
            if (moveFaster && !moveSlower) {
                walkingSpeed /= 8f;
            }
            if (moveSlower && !moveFaster) {
                walkingSpeed *= 10f;
            }
            while (Mouse.next()) {
                if (Mouse.isButtonDown(0)) {
                    Mouse.setGrabbed(true);
                }
                if (Mouse.isButtonDown(1)) {
                    Mouse.setGrabbed(false);
                }

            }
            while (Keyboard.next()) {
                if (Keyboard.isKeyDown(Keyboard.KEY_C)) {
                    position = new Vector3f(0, 0, 0);
                    rotation = new Vector3f(0, 0, 0);
                }
                if (Keyboard.isKeyDown(Keyboard.KEY_O)) {
                    mouseSpeed += 1;
                    System.out.println("Mouse speed changed to " + mouseSpeed + ".");
                }
                if (Keyboard.isKeyDown(Keyboard.KEY_L)) {
                    if (mouseSpeed - 1 > 0) {
                        mouseSpeed -= 1;
                        System.out.println("Mouse speed changed to " + mouseSpeed + ".");
                    }
                }
                if (Keyboard.isKeyDown(Keyboard.KEY_Q)) {
                    System.out.println("Walking speed changed to " + walkingSpeed + ".");
                    walkingSpeed += 1;
                }
                if (Keyboard.isKeyDown(Keyboard.KEY_Z)) {
                    System.out.println("Walking speed changed to " + walkingSpeed + ".");
                    walkingSpeed -= 1;
                }
                if (Keyboard.isKeyDown(Keyboard.KEY_F11)) {
                    try {
                        Display.setFullscreen(!Display.isFullscreen());
                        if (!Display.isFullscreen()) {
                            Display.setResizable(resizable);
                            Display.setDisplayMode(new DisplayMode(800, 600));
                            glViewport(0, 0, Display.getWidth(), Display.getHeight());
                            glMatrixMode(GL_PROJECTION);
                            glLoadIdentity();
                            gluPerspective(fov, (float) Display.getWidth() / (float) Display.getHeight(), zNear, zFar);
                            glMatrixMode(GL_MODELVIEW);
                            glLoadIdentity();
                        } else {
                            glViewport(0, 0, Display.getWidth(), Display.getHeight());
                            glMatrixMode(GL_PROJECTION);
                            glLoadIdentity();
                            gluPerspective(fov, (float) Display.getWidth() / (float) Display.getHeight(), zNear, zFar);
                            glMatrixMode(GL_MODELVIEW);
                            glLoadIdentity();
                        }
                    } catch (LWJGLException ex) {
                        Logger.getLogger(ModelDemo.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
                    if (!Mouse.isGrabbed() || Display.isFullscreen()) {
                        running = false;
                    } else {
                        Mouse.setGrabbed(false);
                    }
                }
            }
            if (resizable) {
                if (Display.wasResized()) {
                    glViewport(0, 0, Display.getWidth(), Display.getHeight());
                    glMatrixMode(GL_PROJECTION);
                    glLoadIdentity();
                    gluPerspective(fov, (float) Display.getWidth() / (float) Display.getHeight(), zNear, zFar);
                    glMatrixMode(GL_MODELVIEW);
                    glLoadIdentity();
                }
            }
            if (printFPS) {
                updateFPS();
            }
            Display.update();
            if (vsync) {
                Display.sync(60);
            }
            if (Display.isCloseRequested()) {
                running = false;
            }
            // </editor-fold>
        }
        glDeleteLists(objectDisplayList, 1);
        Display.destroy();
        System.exit(0);
    }
}