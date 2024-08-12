#!/usr/bin/perl

if (!defined($ARGV[0])) {
	print "\nPlease specify the MIPS flat complexes file as the first argument.\n\n";
	print "The file can be downloaded from\n";
	print "ftp://ftpmips.gsf.de/yeast/catalogues/complexcat/\n";
	print "Filename: complexcat_data_DDMMYYYY\n\n";
	exit;
}

$file = $ARGV[0];

open (FILE, $file) or die "NOO";

%hash = ();
while ($line = <FILE>) {
	# get information from that line
	$line =~ /(.*?)\|(.*?)\|/;
	$prot = $1;
	$id = $2;
	# ids starting with 550 are excluded
	if (!($id =~ /550\.*/)) {
		# add to the array in the hash
		if (!defined($hash{$id})) {
			$hash{$id} = [];
		}
		push @{$hash{$id}}, $prot;
	}
}

# iterate over results
while (($key, $value) = each(%hash)){
	if (scalar(@{$value}) > 1) { # ignore singletons
		foreach my $protid (@{$value}){
			print $protid. "\t";
		}
		print "\n";
	}
}

close(FILE);