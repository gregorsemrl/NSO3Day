package com.cisco.nso.ipam.util;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.cisco.nso.ipam.Subnet;

/**
 * An InetAddressRange consists of two InetAddresses.
 *
 * The addresses are inclusive, i.e.:
 *
 *   (192.168.1.2,192.168.1.2) consists of one IP address.
 *   (192.168.1.2,192.168.1.3) consists of two IP addresses.
 *
 *   Ranges are normalized, with "lower" and "higher".
 *
 *   Both must be the same kind, either IPv4 or IPv6.
 *
 *   This is an immutable object.
 *
 * @author mlutton
 *
 */
public class InetAddressRange implements Comparable<InetAddressRange> {

    private final InetAddress lowerAddress;
    private final InetAddress higherAddress;

    /**
     * Instantiate a Comparator for internet addresses.
     */
    private final InetAddressComparator comparator =
        new InetAddressComparator();


    /**
     * Constructor takes two InetAddresses.
     *
     * @param theFirst
     * @param theSecond
     */
    public InetAddressRange(InetAddress theFirst, InetAddress theSecond) {
        if (comparator.compare(theFirst, theSecond) < 0) {
            lowerAddress = theFirst;
            higherAddress = theSecond;
        } else {
            lowerAddress = theSecond;
            higherAddress = theFirst;
        }
    }

    /**
     * Constructor takes two byte arrays.
     *
     * @param theFirst
     * @param theSecond
     * @throws UnknownHostException if byte arrays are of illegal length
     */
    public InetAddressRange(byte [] theFirst, byte [] theSecond)
        throws UnknownHostException {
        this(InetAddress.getByAddress(theFirst),
             InetAddress.getByAddress(theSecond));

    }

    /**
     * Turns a Subnet into an InetAddressRange.
     *
     * @param subnet
     */
    public InetAddressRange(Subnet subnet) {
        this(subnet.getAddress(), subnet.getBroadcast());
    }

    /**
     * Return the lower of the two addresses.
     * @return lower address.
     */
    public InetAddress getStart() {
        return lowerAddress;
    }

    /**
     * Return the higher of the two addresses.
     *
     * @return higher address.
     */
    public InetAddress getEnd() {
        return higherAddress;
    }

    public BigInteger getSize() {
        BigInteger lowerValue =
            comparator.unsignedBytesToBigInteger(lowerAddress.getAddress());
        BigInteger higherValue =
            comparator.unsignedBytesToBigInteger(higherAddress.getAddress());
        return higherValue.subtract(lowerValue);
    }


    /**
     * For purposes of sorting InetAddressRanges in order, and for storing
     * in a SortedSet, InetAddressRange.compareTo() works similarly to
     * comparing Strings:
     *
     * If A.getStart() < B.getStart() then A < B.
     * If A.getStart() > B.getStart() then A > B.
     * If A.getStart() == B.getStart() then the one with the larger size
     *                                      is greater.
     *
     * @param other
     * @return
     */
    public int compareTo(InetAddressRange other) {

        if (this.getStart().equals(other.getStart())) {
            return comparator.compare(this.getEnd(), other.getEnd());
        } else {
            return comparator.compare(this.getStart(), other.getStart());
        }

    }

    /**
     * Two address ranges overlap if and only if the number of address in the
     * combined range is less than the sum of the number of address in the
     * two ranges.  That is: they overlap if the start address or end address
     * of either is within the range of the other.
     *
     * @param other The other address range.
     * @return true if the two ranges overlap.
     */
    public boolean overlaps(InetAddressRange other) {
        // True if start or end address of either is contained in the other.
        return this.contains(other.getStart()) ||
            this.contains(other.getEnd()) ||
            other.contains(this.getStart()) ||
            other.contains(this.getEnd());
    }

    /**
     * Two address ranges are adjacent if either the end of the first
     * plus one equals the start of the second or the end of the
     * second plus one equals the start of the first.
     *
     * @param other  The other address range
     * @return true    if the two ranges are adjacent.
     */
    public boolean isAdjacentTo(InetAddressRange other) {
        return (comparator.addOne(this.getEnd()).equals(other.getStart())) ||
            (comparator.addOne(other.getEnd()).equals(this.getStart()));
    }

    /**
     * If these two ranges overlap, return a range that represents the union of
     * the addresses.
     *
     * Note:  If the two ranges do NOT overlap, then this will return the union
     * plus everything in between (probably not useful).
     *
     * @param other The other address range.
     * @return       The union of the two (plus everything in between).
     */
    public InetAddressRange combine(InetAddressRange other) {
        InetAddress lower = this.getStart();
        if (comparator.compare(lower, other.getStart()) > 0) {
            // The other has a lower start address.
            lower = other.getStart();
        }
        InetAddress upper = this.getEnd();
        if (comparator.compare(upper, other.getEnd()) < 0) {
            // The other has a higher end address.
            upper = other.getEnd();
        }
        return new InetAddressRange(lower, upper);
    }

    /**
     * Given another InetAddressRange, return a list of ranges such that
     * all the addresses in the returned list are in this range and not
     * in the other range.
     *
     * This will be an empty list if the ranges are disjoint or the second
     * contains the first entirely, a list of
     * two ranges if the second list splits the first, or a list of
     * one range.
     *
     * @param other
     * @return
     */
    public InetAddressRange[] difference(InetAddressRange other) {

        // If equal, difference is nothing.
        // If other contains this entirely, then every address
        // in this is also in other, and thus difference is empty.
        if (this.equals(other) || other.containsAll(this)) {
            return new InetAddressRange[0];
        }

        // If no overlap, difference is this range.
        if (!this.overlaps(other)) {
            InetAddressRange[] result = new InetAddressRange[1];
            result[0] = this;
            return result;
        }

        // Remaining possibilities:
        // 1. other overlaps the lower range of this.  Result is one range.
        // 2. other overlaps the upper range of this.  Result is one range.
        // 3. other splits this into two.  Result is two ranges.

        // Reduce range of other to within this.
        InetAddress otherLow = other.getStart();
        if (comparator.compare(otherLow, this.getStart()) < 0) {
            otherLow = this.getStart();
        }

        InetAddress otherHigh = other.getEnd();
        if (comparator.compare(otherHigh, this.getEnd()) > 0) {
            otherHigh = other.getEnd();
        }

        // We know it is not true that otherLow == this.getStart() &&
        //                             otherLow == this.getEnd();

        // 1. Other range starts at this.  Result is one greater than
        //    other range end, up through this end.
        if (this.getStart().equals(otherLow)) {
            InetAddressRange[] result = new InetAddressRange[1];
            result[0] = new InetAddressRange(comparator.addOne(otherHigh),
                                             this.getEnd());
            return result;
        }



        // 2. Other range ends at this.  Result is this start, up through
        //    one less than other range start.
        if (this.getEnd().equals(otherHigh)) {
            InetAddressRange[] result = new InetAddressRange[1];
            result[0] = new InetAddressRange(this.getStart(),
                                             comparator.subtractOne(otherLow));
            return result;
        }

        // 3. Split into two ranges.  First is this start through one
        //    less than other start; second one greater than other end
        //    through this end.
        InetAddressRange[] result = new InetAddressRange[2];
        result[0] = new InetAddressRange(this.getStart(),
                                         comparator.subtractOne(otherLow));
        result[1] = new InetAddressRange(comparator.addOne(otherHigh),
                                         this.getEnd());
        return result;


    }

    /**
     * An InetAddressRange contains an address if the address is within the
     * start and end addresses inclusive.
     *
     * @param address
     * @return True if the range contains the address.
     */
    public boolean contains(InetAddress address) {

        // Return true if range start is less than or equal to this address
        // and if this address is less than or equal to range end.
        return
            (comparator.compare(this.getStart(), address) <= 0) &&
            (comparator.compare(address, this.getEnd()) <= 0);
    }

    /**
     * This InetAddressRange contansAll(other range) if every address
     * in the other range is also in this range.  True if this range
     * contains the first and last address of the other.
     *
     * @param other Other range.
     * @return true if there is no address in the other that is not in this.
     */
    public boolean containsAll(InetAddressRange other) {
        return this.contains(other.getStart()) && this.contains(other.getEnd());
    }

    /**
     * Return true if this InetAddressRange completely encompasses the
     * range of addresses in the given subnet.
     *
     * @param subnet  An InclusiveSubnet, including the two addresses.
     * @return
     */
    public boolean containsAll(InclusiveSubnet subnet) {
        return containsAll(subnet.toInetAddressRange());
    }

    /**
     * Calculate the largest CIDR such that if you created a subnet with
     * address = getStart() and that CIDR, the address of the subnet would
     * equal getStart().
     *
     * For IPv4, for an address ending in 1111 that's 32, ending in
     * 1110 that's 31, and so on.
     *
     * @return
     */
    public int cidrForStartAddr()
    {
        byte [] asBytes = getStart().getAddress();
        int result = 8 * asBytes.length;

        // Count backwards.  Stop at first non=zero.
        for (int i = asBytes.length - 1; i >= 0; i--) {
            byte oneByte = asBytes[i];
            byte mask = 1;  // We will shift left until shifted out.
            while (mask != 0) {
                if ((oneByte & mask) != 0) {
                    return result;      // Found the first 1 bit.
                }
                result--;
                mask <<= 1;
            }
        }

        return 0;
    }

    /**
     * Two IP Address ranges are equal if the lower and the higher are equal.
     */
    @Override
        public boolean equals(Object otherObject) {
        if (this == otherObject) return true;
        if (otherObject == null) return false;
        if (this.getClass() != otherObject.getClass()) return false;
        InetAddressRange other = (InetAddressRange) otherObject;
        return this.getStart().equals(other.getStart()) &&
            this.getEnd().equals(other.getEnd());
    }

    /**
     * Required by contract of equals()
     */
    @Override
        public int hashCode() {
        int result = 17;
        result = 37 * result + this.getStart().hashCode();
        result = 37 * result + this.getEnd().hashCode();
        return result;
    }

    @Override
        public String toString() {
        // For ease of debugging
        StringBuilder sb = new StringBuilder("[start=");
        sb.append(getStart().toString()).append(",end=")
            .append(getEnd().toString()).append("]");
        return sb.toString();


    }

}
