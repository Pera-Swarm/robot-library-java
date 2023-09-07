package swarm.robot.types;

import swarm.robot.exception.RGBColorException;

public class RGBColorType {
    private int R, G, B;

    /**
     * RGBColorType class
     * 
     * @param R Red intensity, [0,255]
     * @param G Green intensity, [0,255]
     * @param B Blue intensity, [0,255]
     */
    public RGBColorType(int R, int G, int B) {
        this.R = validate(R);
        this.G = validate(G);
        this.B = validate(B);
    }

    /**
     * RGBColorType class
     * 
     * @param color int[3], where colors are in order {R,G,B}
     */
    public RGBColorType(int[] color) {
        if (!(color.length == 3 || color.length == 4)) {
            // ambient will be ignored
            throw new IllegalArgumentException("length of the color[] should be equal to 3");
        } else {
            setColor(validate(color[0]), validate(color[1]), validate(color[2]));
        }
    }

    /**
     * RGBColorType class
     * 
     * @param str string, where colors are in order "R G B"
     */
    public RGBColorType(String str) {
        this.setColor(str);
    }

    /**
     * Set a color
     * 
     * @param str string, where colors are in order "R G B"
     */
    public void setColor(String str) {
        String[] color = str.split(" ");

        if (!(color.length == 3 || color.length == 4)) {
            try {
                throw new RGBColorException("setColor(str) length mismatch");
            } catch (RGBColorException e) {
                e.printStackTrace();
            }
        }
        // ambient will be ignored
        setColor(validate(color[0]), validate(color[1]), validate(color[2]));
    }

    /**
     * Set a color using hexadecimal code
     * 
     * @param hexCode string, where colors are in format like "#00AAFF"
     */
    public void setColorFromHexCode(String hexCode) {
        this.R = validate(Integer.valueOf(hexCode.substring(1, 3), 16));
        this.G = validate(Integer.valueOf(hexCode.substring(2, 5), 16));
        this.B = validate(Integer.valueOf(hexCode.substring(5, 7), 16));
    }

    /**
     * Set a color
     * 
     * @param str string, where colors are in order {R,G,B}
     */
    public void setColor(int R, int G, int B) {
        this.R = validate(R);
        this.G = validate(G);
        this.B = validate(B);
    }

    /**
     * Validate a given string to be color value
     * 
     * @param str color value, ["0","255"]
     * @throws RGBColorException
     * @see int validate(int i)
     */
    private int validate(String s) {
        return validate(Integer.parseInt(s));
    }

    /**
     * Validate a given string to be color value
     * 
     * @param int color value, [0,255]
     * @return int color value
     * @throws RGBColorException
     */
    private int validate(int i) {
        if (i < 0 || i > 255) {
            try {
                throw new RGBColorException(R, G, B);
            } catch (RGBColorException e) {
                e.printStackTrace();
            }
        }
        return i;
    }

    /**
     * get R component of the color
     * 
     * @return R component
     */
    public int getR() {
        return this.R;
    }

    /**
     * get G component of the color
     * 
     * @return G component
     */
    public int getB() {
        return this.B;
    }

    /**
     * get B component of the color
     * 
     * @return B component
     */
    public int getG() {
        return this.G;
    }

    /**
     * get color as an array of R,G,B components
     * 
     * @return int[] {R, G, B}
     */
    public int[] getColor() {
        return new int[] { this.R, this.G, this.B };
    }

    /**
     * return color components as a string
     * 
     * @return string "R:#, G:#, B:#"
     */
    public String toString() {
        return "R:" + this.R + ", G:" + this.G + ", B:" + this.B;
    }

    /**
     * return color components as a string
     * 
     * @return string with format "# # #"
     */
    public String toStringColor() {
        return this.R + " " + this.G + " " + this.B;
    }

    /**
     * compare the color with another RGBColorType object
     * 
     * @param color RGBColorType
     * @return boolean
     */
    public boolean compareTo(RGBColorType color) {
        return (color.getR() == this.R) && (color.getG() == this.G) && (color.getB() == this.B);
    }
}
