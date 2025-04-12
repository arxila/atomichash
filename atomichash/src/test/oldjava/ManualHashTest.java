package io.aquen.atomichash;

import java.util.Arrays;
import org.apache.commons.lang3.RandomStringUtils;

public class ManualHashTest {

    private static RandomStringUtils RANDOM_STRING_UTILS = RandomStringUtils.insecure();



    public static void main(String[] args) {

        for (int iter = 0; iter < 10; iter++) {
            final Object[] structure = createStructure(0);
            for (final String str : createRandomStrings(10)) {
                put(structure, 0, AtomicHashStore.hash(str));
            }
            Object[] simplifiedStructure = (Object[]) simplifyTree(structure);
            System.out.println(Arrays.deepToString(simplifiedStructure));
        }

    }

    private static Object[] createStructure(final int level) {
        final Object[] structure = new Object[AtomicHashStore.arraySizeFor(level)];
        for (int i = 0; i < structure.length; i++) {
            if (level < 7) {
                structure[i] = createStructure(level + 1);
            } else {
                structure[i] = Integer.valueOf(0);
            }
        }
        return structure;
    }

    private static void put(final Object[] structure, final int level, final int hash) {
        final int pos = AtomicHashStore.pos(level, hash);
        if (level < 7) {
            put(((Object[])structure[pos]), level + 1, hash);
        } else {
            structure[pos] = Integer.valueOf(((Integer)structure[pos]).intValue() + 1);
        }
    }


    private static Object simplifyTree(final Object[] tree) {
        final Object[] simplified = new Object[tree.length];
        int values = 0;
        for (int i = 0; i < tree.length; i++) {
            simplified[i] = tree[i];
            if (tree[i] instanceof Object[]) {
                simplified[i] = simplifyTree((Object[])tree[i]);
            }
            if (simplified[i] instanceof Integer) {
                values += ((Integer)simplified[i]).intValue();
            } else {
                values += 2; // So that this array cannot be simplified
            }
        }
        if (values == 0 || values == 1) {
            return Integer.valueOf(values);
        }
        return simplified;
    }


    private static String[] createRandomStrings(int count) {
        String[] strings = new String[count];
        for (int i = 0; i < count; i++) {
            strings[i] = "ALWAYSTHESAME"+RANDOM_STRING_UTILS.nextAlphabetic(1, 3);
        }
        return strings;
    }


}
