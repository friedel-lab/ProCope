ProCope
http://www.bio.ifi.lmu.de/Complexes/ProCope/

The data/ folder contains complex sets, localization data, purification 
datasets, PPI scores networks and name mappings from different sources.

Note that file names tagged with a datestamp in the format YYDDMM are datasets
extracted from databases and might change.



Below you find detailed descriptions of the datasets contained in this folder 
along with references to the respective publications.

complexes/aloy_complexes.txt     Complexes from structure-based assembly [1]
complexes/BT_217.txt             Very high confidence bootstrap complexes [2]
complexes/BT_409.txt             High confidence bootstrap complexes [2]
complexes/BT_893.txt             Bootstrap complexes [2]
complexes/gavin_all.txt          All complexes from [3]
complexes/gavin_core.txt         Only "core" complexes from [3]
complexes/gavin_coremod.txt      "Core" and "Module" complexes from [3]
complexes/hart_complexes.txt     Complexes derived by [4]
complexes/krogan_complexes.txt   Complexes derived by [5]
complexes/mips_complexes.txt     Gold-standard set of complexes [6]
complexes/mips_s50.txt           MIPS set with the two largest complexes removed
complexes/get_mips_complexes.pl  Perl-Parser to extract complexes from the MIPS database
                                 (start without any arguments for instructions)
go/
Contains the Gene Ontology along with annotations for S. cerevisiaa.
The most recent versions are availabe at: 
http://www.geneontology.org/GO.downloads.shtml


localization/huh_loc_YYDDMM.txt     Localization data by [9]
localization/kumar_loc_YYDDMM.txt   Localization data by [10]

purifications/gavin_raw.txt             Purification data by [3]
purifications/gavinkrogan_combined.txt  Purification data of [3] and [5] combined
purifications/ho_raw.txt                Purification data by [7]
purifications/krogan_highconf.txt       High confidence data by [5] (see below)
purifications/krogan_raw.txt            Purification data by [5]

Note: The high confidence dataset of Krogan et al. [5] contains only those
purifications where LCMS or MALDI purification confidence is >= 99.6 or
>= 3.4 respectively.

scores/bootstrap_combined.txt.gz    PPI network by [2]
scores/conf_krogan.txt.gz           Confidence scores PPI network by [5]
scores/hart_scores.txt.gz           PPI network by [4]
scores/pe_combined.txt.gz           PPI network by [8] with data from [3] and [5]
scores/pe_gavin.txt.gz              PPI network by [8] with data from [3]
scores/pe_krogan.txt.gz             PPI network by [8] with data from [5]
scores/socios.txt.gz                Socio affinity scores network by [3]

README.txt            this file

yeastmappings_YYDDMM.txt  

A file which contains mappings of different type of S. cerevisiae gene and 
protein identifiers to the "Systematic name" (e.g. YAL016W). Parsed from the
Uniprot KB for yeast and a name mapping file from the SGD. Should contain 
mappings from:
 - Uniprot identifiers (e.g. P31383)
 - Primary SGDID (e.g. S000000014)
 - Gene standard names (e.g. TPD3) 

References
----------
 [1] Aloy et al., 2004      http://view.ncbi.nlm.nih.gov/pubmed/15044803
 [2] Friedel et al., 2008   http://www.bio.ifi.lmu.de/Complexes/
 [3] Gavin et al., 2006     http://www.ncbi.nlm.nih.gov/pubmed/16429126
 [4] Hart et al., 2007      http://www.ncbi.nlm.nih.gov/pubmed/17605818
 [5] Krogan et al., 2006    http://www.ncbi.nlm.nih.gov/pubmed/16554755
 [6] Mewes et al., 2004     http://www.ncbi.nlm.nih.gov/pubmed/14681354
     Data downloaded from:  ftp://ftpmips.gsf.de/yeast/catalogues/complexcat/
 [7] Ho et al., 2002        http://www.ncbi.nlm.nih.gov/pubmed/11805837
 [8] Collins et al., 2007   http://www.ncbi.nlm.nih.gov/pubmed/17200106
 [9] Huh et al., 2003       http://www.ncbi.nlm.nih.gov/pubmed/14562095
     Data downloaded from:  http://yeastgfp.ucsf.edu/
[10] Kumar et al., 2002     http://www.ncbi.nlm.nih.gov/pubmed/11914276
     Data downloaded from:  http://ygac.med.yale.edu/triples/  
    