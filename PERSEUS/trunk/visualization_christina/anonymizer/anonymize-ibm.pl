: # Use perl
   eval 'exec perl -w -S $0 "$@"'
   if 0;

# it is faster to use something like
#!/usr/local/bin/perl5 
# but this is more portable.

# expects a blank-separated file
# and replaces the specified column(s)
# with short, unique numbers
# Important flags:
#     -d	-> things are stored on disk
#     -n<dbname>   and specifically on the <dbname>.dbm and r<dbname>.dbm
# (otherwise, they are dropped)

# TO DO:
# DONE 1) use 'ties' to handle memory overflows
#      2) make a 'harness'
#      3) permit comments within the lines of the input
#      4) re-construct the anonymized table, given the look-ups
###########################################

#
# $Log: anonymize.pl,v $
# Revision 1.5  2003/12/24 04:41:58  falouts
# *** empty log message ***
#
# Revision 1.10  2003/12/24 04:10:12  faloutsos
# *** empty log message ***
#
# Revision 1.9  2003/12/24 03:53:49  faloutsos
# *** empty log message ***
#
# Revision 1.8  2003/12/24 03:31:54  faloutsos
# added support for dbm files (straight and reverse)
#
# Revision 1.7  2003/12/24 03:01:57  faloutsos
# *** empty log message ***
#
# Revision 1.6  2003/12/23 13:53:28  faloutsos
# *** empty log message ***
#
# Revision 1.5  2003/12/23 12:04:09  faloutsos
# can huses ties
#
# Revision 1.1  2003/12/23 06:55:37  falouts
# *** empty log message ***
#
# Revision 1.3  2003/11/06 10:15:42  faloutsos
# tested it; made it works for 2more than one stringcolumnsmns
#
# Revision 1.2  2003/11/06 10:04:25  faloutsos
# *** empty log message ***
#
# Revision 1.1  2003/11/06 09:07:52  faloutsos
# Initial revision
#
#


use strict;

my $verbose = 0;
my $debug = 0;
my $helpmsg = "USAGE: $0 [-h] [-v] [-c<colunm,column...>]" .
   "[-d] " .
   "[-n<dbname>] " .
   "[-p<pfname>] <fname>\n" .
   "    column numbering starts from ONE\n";
my $colstring = "1"; # by default, the first column
my @cols;      # list of columns to anonymize, TOGETHER
my $col;
my $printout = 0; # flag to printout the lookup table
my $pfile; # name of printout file
my $onDisk=0; # by default, in memory
my $dictname="_" . $$ ; # name of dbm file
my $mkDict=0;


my $nargs=0;

while( $_ = $ARGV[0], /^-/){
   shift;
   last if /^--$/;
   if( /^-D(.*)/){ $debug = $1 }
   if( /^-c(.*)/){ $colstring = $1 }
   if( /^-n(.*)/){ 
       $dictname = $1; 
       $onDisk = 1; 
   }
   if( /^-h/){ print $helpmsg ; exit; }
   if (/^-v/)    { $verbose++ }
   if (/^-d/)    { $onDisk=1 }
   if (/^-p(.*)/)    { $printout = 1; $pfile = $1;}
}

@cols = split(",", $colstring);
foreach $col ( @cols ){
   # $col = int ( $col + 0 ); # rounding, just in case
   if( $col <= 0 ) { die "illegal value for column number: $col - exiting\n"; }
}


if( $verbose >= 1){
    warn "*** verbose=", $verbose, "\n";
    warn "*** columns= ", join(": ", @cols), "\n";
    warn "*** printout = ", $printout, "\n";
}

if( $onDisk > 0 ){
    use DB_File;
}


########### code starts here ######
my %tagged=(); # to tag the columns we want to anonymize
foreach $col ( @cols ){
    $tagged{$col} = 1;
}

my %id  = (); #clear the look-up table
my %rid = (); #clear the reverse-look-up table
my $hfname;
my $rhfname; # for the reverse hash

# if( $mkDict ) {
    # $hfname = $dbname . ".dbm";
    # $rhfname = $dbname . "_r" . ".dbm";
# else{
    # $hfname = "_" . $$ . "_ids.dbm";
# }

if( $onDisk ){

    $hfname = $dictname . ".dbm";
    $rhfname = $dictname . "_r.dbm";
    tie( %id, "DB_File", $hfname, O_RDWR|O_CREAT, 0666, $DB_BTREE)
     or die "can't tie $hfname:$!";

    tie( %rid, "DB_File", $rhfname, O_RDWR|O_CREAT, 0666, $DB_BTREE)
     or die "can't tie $rhfname:$!";

    # make sure they are both empty
    %id = ();
    %rid = ();
}

my $i = 0;       #counter of distinct ids

my $val;
my $len;
# my $colminus = $col -1; # since arrays start from zero index...
my @words;
my $lno = 0; # line number
my $lenfirst; # number of columns for the first row
my $colminus; # = $col - 1, to make them start from ZERO
my $outval;
my @outlist=();
my $lastId= 0;

while(<>){
   chomp($_);
   s/^\s+//; s/\s+$//; # remove leading and trailing spaces

   $lno ++;

   @words = split  ;
   $len = scalar( @words );
   if ($lno >1) { #check for irregular number of columns
       if( $len != $lenfirst ) {
           print "ERROR: line $lno has $len cols, vs $lenfirst - exiting\n";
	   exit;
       }
   }
   if( $lno == 1) { $lenfirst = $len ;} # set the 'width'
   if ($verbose) { print "len = ", $len , "\n"; }
   # if ($col > $len) { 
       # print "ERROR: line $lno has only $len columns; $col needed - exiting \n";
       # exit;
   # }


   # scan every value, and anonymize it if necessary

   @outlist =();
   for( $col=1; $col <= $len; $col++){
       $colminus = $col -1;
       if( $tagged{$col} ){ # then anonymize its value
           $outval = anon( $words[$colminus] );
       }else{
           $outval = $words[$colminus];
       }
       @outlist = ( @outlist, $outval);
    }


   print join( " ", @outlist), "\n";
}

if ($printout == 1) { #print the lookup table
   use Fcntl;
   # open(FH, "> $pfile") or die "can't open $pfile: $!";
   sysopen(FH, $pfile, O_WRONLY|O_EXCL|O_CREAT ) or 
       die "can't open $pfile: $!";

   foreach $val (keys %id){
      print FH $val, "\t", $id{$val}, "\n";
   }
   close(FH);
   warn "lookup table for ", join("," , @cols), " is in: '$pfile'\n";
}

if($onDisk){
    untie %id;
    untie %rid;
    if( $dictname eq ("_". $$)){ # drop the lookups
        unlink $hfname or die "could not unlink $hfname: $!\n";
        unlink $rhfname or die "could not unlink $rhfname: $!\n";
    }
}

# anonymizes the given value
sub anon{
    my $val;
    ($val) = @_;
    my $outval;

    if( not defined($id{$val} )) {
        $lastId ++;
	$id{$val} = $lastId;
	if($onDisk){
	    $rid{$lastId} = $val; # fix the reverse lookup, too
        }
    }
    return($id{$val});
}

