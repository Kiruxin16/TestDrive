package api;

import java.util.Random;

public class Utilities {

    public static String generateRegData(){
        Random random =new Random();
        char[] nameGen= new char[random.nextInt(9)+3];
        for (int i = 0; i <nameGen.length ; i++) {
            switch (random.nextInt(3) + 1) {
                case (1) -> nameGen[i] = (char) (random.nextInt(25) + 97);
                case (2) -> nameGen[i] = (char) (random.nextInt(25) + 65);
                case (3) -> nameGen[i] = (char) (random.nextInt(10) + 48);
            }
        }
        return new String(nameGen);

    }
}
