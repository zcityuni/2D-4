1. Test Networks

Both test networks are up and running!  For various reasons outside of
my control both of the machines that are running them are somewhat
fragile so please be aware that there might be some downtime over the
next little while.  I will do everything I can to keep them running as
smoothly as possible.

One test network is on the Azure virtual lab.  It has 11 nodes with
the following names / addresses:

martin.brain@city.ac.uk:martins-implementation-1.0,fullNode-20000
10.0.0.164 20000

martin.brain@city.ac.uk:martins-implementation-1.0,fullNode-20001
10.0.0.164 20001

martin.brain@city.ac.uk:martins-implementation-1.0,fullNode-20002
10.0.0.164 20002

and so on.  If you are using your lab machine and have your code
compiled and working then you should be able to use the command line
programs to do some system-level testing.

To test the temporary node you can run:

java CmdLineGet martin.brain@city.ac.uk:martins-implementation-1.0,fullNode-20000 10.0.0.164:20000 test/jabberwocky/1

this should get you the first verse of a poem.  If you change the key
to test/jabberwocky/2 and so on then you should get the whole poem.
This should work regardless which of the nodes you use as the first
node.

You can also run:

java CmdLineStore martin.brain@city.ac.uk:martins-implementation-1.0,fullNode-20000 10.0.0.164:20000 YOUR_EMAIL_ADDRESS Working
java CmdLineGet martin.brain@city.ac.uk:martins-implementation-1.0,fullNode-20000 10.0.0.164:20000 YOUR_EMAIL_ADDRESS

( with your e-mail address replacing the string YOUR_EMAIL_ADDRESS
obviously! ) which will test storing as well and let anyone else who
looks know that you are doing well on the coursework.  Again, this
should work starting with any node.

Finally, when you have the full node working you could run:

java CmdLineFullNode martin.brain@city.ac.uk:martins-implementation-1.0,fullNode-20000 10.0.0.164:20000 YOUR_LAB_MACHINE_IP 20000

and your program will be included in the network of full nodes.  (
Again, you will need to put the IP address of your lab machine instead
of YOUR_LAB_MACHINE_IP .  Remember Practical 4?)



The other test network is running on 2dh4.city.ac.uk .  To access this
you must be inside the City network, which includes:

* The computers in the labs.
* The City virtual desktop machines ( https://studenthub.city.ac.uk/information-technology/city-virtual-desktop )
* Computers connected to the new City VPN ( https://cityuni.service-now.com/sp?id=kb_article_view&sys_kb_id=c1ac71ba1b3c82104e86b886d34bcbdf please note this is managed by IT; you will have to ask them if you need support )

I believe the firewalling issues have been resolved but it is possible
there may still be some restrictions.  Let me know if there are issues.

Again there are 11 nodes and the names are the same but the IP addresses are different:

martin.brain@city.ac.uk:martins-implementation-1.0,fullNode-20000
10.200.51.65 20000

martin.brain@city.ac.uk:martins-implementation-1.0,fullNode-20001
10.200.51.65 20001

martin.brain@city.ac.uk:martins-implementation-1.0,fullNode-20002
10.200.51.65 20002

All of the same tests should work.

https://pastebin.com/q58kMF7e
