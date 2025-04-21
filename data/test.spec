    /* A region of DNA is maintained as a tuple of four components:

		the contig
		the beginning position (from 1)
		the strand
		the length

	We often speak of "a region".  By "location", we mean a sequence
	of regions from the same genome (perhaps from distinct contigs).

	Strand is either '+' or '-'.
     */
