//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2010 Michael Bayne, et al.
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.samskivert.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Provides utility routines to simplify obtaining randomized values.
 *
 * Each instance of Randoms is completely thread safe, but will share an underlying
 * {@link Random} object. If you wish to have a private stream of pseudorandom numbers,
 * use the {@link #with} factory.
 */
public class Randoms
{
    /** A default Randoms that can be safely shared by any caller. */
    public static final Randoms RAND = with(new Random());

    /**
     * A factory to create a new Randoms object.
     */
    public static Randoms with (Random rand)
    {
        return new Randoms(rand);
    }

    /**
     * Get a thread-local Randoms instance that will not contend with any other thread
     * for random number generation.
     *
     * <p><b>Note:</b> while all Randoms instances are thread-safe, normally they use a
     * java.util.Random internally that must protect against multiple threads generating
     * psuedorandom numbers with it simultaneously. This method will return a Randoms
     * that uses an internal Random subclass with no such safeguards, resulting in much
     * less overhead. However, you should probably not store a reference to the result,
     * but instead always use it immediately as in the following example:
     * <pre style="code">
     *     Puppy pick = Randoms.threadLocal().pick(Puppy.LITTER, null);
     * </pre>
     */
    public static Randoms threadLocal ()
    {
        return _localRandoms.get();
    }

    /**
     * Returns a pseudorandom, uniformly distributed <code>int</code> value between 0 (inclusive)
     * and the specified value (exclusive).
     *
     * @param high the high value limiting the random number sought.
     *
     * @throws IllegalArgumentException if <code>high</code> is not positive.
     */
    public int getInt (int high)
    {
        return _r.nextInt(high);
    }

    /**
     * Returns a pseudorandom, uniformly distributed <code>int</code> value between
     * <code>low</code> (inclusive) and <code>high</code> (exclusive).
     *
     * @throws IllegalArgumentException if <code>high - low</code> is not positive.
     */
    public int getInRange (int low, int high)
    {
        return low + _r.nextInt(high - low);
    }

    /**
     * Returns a pseudorandom, uniformly distributed float value between 0.0 (inclusive) and the
     * specified value (exclusive).
     *
     * @param high the high value limiting the random number sought.
     */
    public float getFloat (float high)
    {
        return _r.nextFloat() * high;
    }

    /**
     * Returns a pseudorandom, uniformly distributed <code>float</code> value between
     * <code>low</code> (inclusive) and <code>high</code> (exclusive).
     */
    public float getInRange (float low, float high)
    {
        return low + (_r.nextFloat() * (high - low));
    }

    /**
     * Returns true approximately one in n times.
     *
     * @throws IllegalArgumentException if <code>n</code> is not positive.
     */
    public boolean getChance (int n)
    {
        return (0 == _r.nextInt(n));
    }

    /**
     * Has a probability p of returning true.
     */
    public boolean getProbability (float p)
    {
        return _r.nextFloat() < p;
    }

    /**
     * Returns true or false with approximately even distribution.
     */
    public boolean getBoolean ()
    {
        return _r.nextBoolean();
    }

    /**
     * Returns a key from the supplied map according to a probability computed as
     * the key's value divided by the total of all the key's values.
     *
     * @throws NullPointerException if the map is null.
     * @throws IllegalArgumentException if the sum of the weights is not positive.
     */
    public <T> T getWeighted (Map<T, Integer> valuesToWeights)
    {
        // TODO: validation?
        int idx = _r.nextInt(Folds.sum(0, valuesToWeights.values()));
        for (Map.Entry<T, Integer> entry : valuesToWeights.entrySet()) {
            idx -= entry.getValue();
            if (idx < 0) {
                return entry.getKey();
            }
        }
        throw new AssertionError("Not possible");
    }

    /**
     * Pick a random element from the specified Iterator, or return <code>ifEmpty</code>
     * if it is empty.
     *
     * <p><b>Implementation note:</b> because the total size of the Iterator is not known,
     * the random number generator is queried after the second element and every element
     * thereafter.
     *
     * @throws NullPointerException if the iterator is null.
     */
    public <T> T pick (Iterator<? extends T> iterator, T ifEmpty)
    {
        if (!iterator.hasNext()) {
            return ifEmpty;
        }
        T pick = iterator.next();
        for (int count = 2; iterator.hasNext(); count++) {
            T next = iterator.next();
            if (0 == _r.nextInt(count)) {
                pick = next;
            }
        }
        return pick;
    }

    /**
     * Pick a random element from the specified Iterable, or return <code>ifEmpty</code>
     * if it is empty.
     *
     * <p><b>Implementation note:</b> optimized implementations are used if the Iterable
     * is a List or Collection. Otherwise, it behaves as if calling {@link #pick(Iterator)} with
     * the Iterable's Iterator.
     *
     * @throws NullPointerException if the iterable is null.
     */
    public <T> T pick (Iterable<? extends T> iterable, T ifEmpty)
    {
        return pickPluck(iterable, ifEmpty, false);
    }

    /**
     * Pluck (remove) a random element from the specified Iterable, or return <code>ifEmpty</code>
     * if it is empty.
     *
     * <p><b>Implementation note:</b> optimized implementations are used if the Iterable
     * is a List or Collection. Otherwise, two Iterators are created from the Iterable
     * and a random number is generated after the second element and all beyond.
     *
     * @throws NullPointerException if the iterable is null.
     * @throws UnsupportedOperationException if the iterable is unmodifiable or its Iterator
     * does not support {@link Iterator#remove()}.
     */
    public <T> T pluck (Iterable<? extends T> iterable, T ifEmpty)
    {
        return pickPluck(iterable, ifEmpty, true);
    }

    /**
     * Construct a Randoms.
     */
    protected Randoms (Random rand)
    {
        _r = rand;
    }

    /**
     * Shared code for pick and pluck.
     */
    protected <T> T pickPluck (Iterable<? extends T> iterable, T ifEmpty, boolean remove)
    {
        if (iterable instanceof Collection) {
            // optimized path for Collection
            Collection<? extends T> coll = (Collection<? extends T>)iterable;
            int size = coll.size();
            if (size == 0) {
                return ifEmpty;
            }
            if (coll instanceof List) {
                // extra-special optimized path for Lists
                List<? extends T> list = (List<? extends T>)coll;
                int idx = _r.nextInt(size);
                return remove ? list.remove(idx) : list.get(idx);
            }
            // for other Collections, we must iterate
            Iterator<? extends T> it = coll.iterator();
            for (int idx = _r.nextInt(size); idx > 0; idx--) {
                it.next();
            }
            try {
                return it.next();
            } finally {
                if (remove) {
                    it.remove();
                }
            }
        }

        if (!remove) {
            return pick(iterable.iterator(), ifEmpty);
        }

        // from here on out, we're doing a pluck with a complicated two-iterator solution
        Iterator<? extends T> it = iterable.iterator();
        if (!it.hasNext()) {
            return ifEmpty;
        }
        Iterator<? extends T> lagIt = iterable.iterator();
        T pick = it.next();
        lagIt.next();
        for (int count = 2, lag = 1; it.hasNext(); count++, lag++) {
            T next = it.next();
            if (0 == _r.nextInt(count)) {
                pick = next;
                for ( ; lag > 0; lag--) {
                    lagIt.next();
                }
            }
        }
        lagIt.remove();
        return pick;
    }

    /** The random number generator. */
    protected final Random _r;

    /** A ThreadLocal for accessing a thread-local version of Randoms. */
    protected static final ThreadLocal<Randoms> _localRandoms = new ThreadLocal<Randoms>() {
        @Override
        public Randoms initialValue () {
            return with(new ThreadLocalRandom());
        }
    };

    /*
     * TODO: This can be updated and this inner class removed
     * when the samskivert library is JDK 1.7 compatible.
     *-----------------------------------------------------------------
     *
     * Written by Doug Lea with assistance from members of JCP JSR-166
     * Expert Group and released to the public domain, as explained at
     * http://creativecommons.org/licenses/publicdomain
     *
     * A random number generator isolated to the current thread.  Like the
     * global {@link java.util.Random} generator used by the {@link
     * java.lang.Math} class, a {@code ThreadLocalRandom} is initialized
     * with an internally generated seed that may not otherwise be
     * modified. When applicable, use of {@code ThreadLocalRandom} rather
     * than shared {@code Random} objects in concurrent programs will
     * typically encounter much less overhead and contention.  Use of
     * {@code ThreadLocalRandom} is particularly appropriate when multiple
     * tasks (for example, each a {@link ForkJoinTask}) use random numbers
     * in parallel in thread pools.
     *
     * <p>This class also provides additional commonly used bounded random
     * generation methods.
     *
     * @since 1.7
     * @author Doug Lea
     */
    protected static class ThreadLocalRandom extends Random {
        // same constants as Random, but must be redeclared because private
        private final static long multiplier = 0x5DEECE66DL;
        private final static long addend = 0xBL;
        private final static long mask = (1L << 48) - 1;

        /**
         * The random seed. We can't use super.seed.
         */
        private long rnd;

        /**
         * Initialization flag to permit calls to setSeed to succeed only
         * while executing the Random constructor.  We can't allow others
         * since it would cause setting seed in one part of a program to
         * unintentionally impact other usages by the thread.
         */
        boolean initialized;

        // Padding to help avoid memory contention among seed updates in
        // different TLRs in the common case that they are located near
        // each other.
        @SuppressWarnings("unused")
        private long pad0, pad1, pad2, pad3, pad4, pad5, pad6, pad7;

        /**
         * Constructor called only by localRandom.initialValue.
         */
        ThreadLocalRandom() {
            super();
            initialized = true;
        }

        /**
         * Throws {@code UnsupportedOperationException}.  Setting seeds in
         * this generator is not supported.
         *
         * @throws UnsupportedOperationException always
         */
        @Override
        public void setSeed(long seed) {
            if (initialized)
                throw new UnsupportedOperationException();
            rnd = (seed ^ multiplier) & mask;
        }

        @Override
        protected int next(int bits) {
            rnd = (rnd * multiplier + addend) & mask;
            return (int) (rnd >>> (48-bits));
        }

// as of JDK 1.6, this method does not exist in java.util.Random
//        /**
//         * Returns a pseudorandom, uniformly distributed value between the
//         * given least value (inclusive) and bound (exclusive).
//         *
//         * @param least the least value returned
//         * @param bound the upper bound (exclusive)
//         * @throws IllegalArgumentException if least greater than or equal
//         * to bound
//         * @return the next value
//         */
//        public int nextInt(int least, int bound) {
//            if (least >= bound)
//                throw new IllegalArgumentException();
//            return nextInt(bound - least) + least;
//        }

        /**
         * Returns a pseudorandom, uniformly distributed value
         * between 0 (inclusive) and the specified value (exclusive).
         *
         * @param n the bound on the random number to be returned.  Must be
         *        positive.
         * @return the next value
         * @throws IllegalArgumentException if n is not positive
         */
        public long nextLong(long n) {
            if (n <= 0)
                throw new IllegalArgumentException("n must be positive");
            // Divide n by two until small enough for nextInt. On each
            // iteration (at most 31 of them but usually much less),
            // randomly choose both whether to include high bit in result
            // (offset) and whether to continue with the lower vs upper
            // half (which makes a difference only if odd).
            long offset = 0;
            while (n >= Integer.MAX_VALUE) {
                int bits = next(2);
                long half = n >>> 1;
                long nextn = ((bits & 2) == 0) ? half : n - half;
                if ((bits & 1) == 0)
                    offset += n - nextn;
                n = nextn;
            }
            return offset + nextInt((int) n);
        }

        /**
         * Returns a pseudorandom, uniformly distributed value between the
         * given least value (inclusive) and bound (exclusive).
         *
         * @param least the least value returned
         * @param bound the upper bound (exclusive)
         * @return the next value
         * @throws IllegalArgumentException if least greater than or equal
         * to bound
         */
        public long nextLong(long least, long bound) {
            if (least >= bound)
                throw new IllegalArgumentException();
            return nextLong(bound - least) + least;
        }

        /**
         * Returns a pseudorandom, uniformly distributed {@code double} value
         * between 0 (inclusive) and the specified value (exclusive).
         *
         * @param n the bound on the random number to be returned.  Must be
         *        positive.
         * @return the next value
         * @throws IllegalArgumentException if n is not positive
         */
        public double nextDouble(double n) {
            if (n <= 0)
                throw new IllegalArgumentException("n must be positive");
            return nextDouble() * n;
        }

        /**
         * Returns a pseudorandom, uniformly distributed value between the
         * given least value (inclusive) and bound (exclusive).
         *
         * @param least the least value returned
         * @param bound the upper bound (exclusive)
         * @return the next value
         * @throws IllegalArgumentException if least greater than or equal
         * to bound
         */
        public double nextDouble(double least, double bound) {
            if (least >= bound)
                throw new IllegalArgumentException();
            return nextDouble() * (bound - least) + least;
        }

        private static final long serialVersionUID = -5851777807851030925L;
    }
}
