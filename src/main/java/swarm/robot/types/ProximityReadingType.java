package swarm.robot.types;

import swarm.robot.exception.ProximityException;

public class ProximityReadingType {

    public ProximityReadingType(int[] angles, String str) throws ProximityException {
        int readingCount = angles.length;
        String[] values = str.split(" ");
        int[] distance = new int[readingCount];
        RGBColorType color[] = new RGBColorType[readingCount];

        if (values.length != readingCount * 2) {
            throw new ProximityException("length mismatch " + values.length);
        }

        for (int i = 0; i < readingCount; i++) {
            System.out.println(">>" + values[i] + " " + values[readingCount + i]);

            // Reading distances
            if (values[i].compareTo("Infinity") == 0) {
                // -1 will be returned as a fail-proof option. Should throw an exception
                System.out.println("Proximity: Infinity reading received for " + i);
                distance[i] = -1;
            } else {
                distance[i] = Integer.parseInt(values[i]);
            }

            // Reading colors
            int[] colorComponents = hexColor2RGB(values[readingCount + i]);
            color[i] = new RGBColorType(colorComponents);

            System.out.println(i + " " + distance[i] + color[i].toString());
        }
    }

    private int[] hexColor2RGB(String hexCode) {

        int red = Integer.valueOf(hexCode.substring(1, 3), 16);
        int green = Integer.valueOf(hexCode.substring(2, 5), 16);
        int blue = Integer.valueOf(hexCode.substring(5, 7), 16);
        int[] response = { red, green, blue };
        return response;
    }

    public String toString() {
        return ""; // readings[0] + " " + readings[1] + " " + readings[2] + " " + readings[3] + " "
                   // + readings[4];
    }
}
