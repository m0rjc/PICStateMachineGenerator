State Machine Generator for PIC18 - Experiment in CDI
=====================================================

This branch exists as an experiment in CDI. CDI provides an intersting
possiblity for plugin handling, but is proving complex in so many other
ways. The biggest impact is that you have to be CDI throught, unless I
can find a way of querying the BeanManager directly in the situations
I need it. This has proven most complex with the callback handlers. CDI
Events seem too global.

CDI makes you think about scopes, and all seems a bit "magical"
how - I hope at least - all my classes that want the current StateModel
will get it (or at least a proxy to it) even though their own lifecycles
are quite different. If I keep to CDI I'll likely lose the custom scope
and have a ThreadLocal context of my own because it's so much easier.
Even if I don't stick to CDI, the work on ChainedSaxHandler is useful
and will likely be merged back to the main branch.

I need to investigate CDI and CDI unit testing at work, so this larger
codebase that is more meaningful than "Hello World" gives a good opportunity.
The biggest difference perhaps is that this is a standalone application with 
complex logic. I work with web technologies where CDI scopes and lifecycles are more
obviously useful.

