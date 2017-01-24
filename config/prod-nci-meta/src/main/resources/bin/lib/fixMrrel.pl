#!/usr/bin/perl
use Digest::MD5  qw(md5_hex);
while (<>) {
	@_ = split /\|/;
	$x = $_[8];
	$_[8] = "abc";
	$hashcode = md5_hex(join "|",@_);
	$_[8] = $x;
	if (!$hash{$hashcode}) {
		print;
		$hash{$hashcode}=1;
	}
}
