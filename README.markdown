State Machine Generator for PIC18
=================================

This code is branched out of my M0RJCTracker repository. It is becoming
big and, with a little work, will become sufficiently generalised to
be useful in its own right.

The state machines are intended for parsing input, for example coming over
a serial port from a GPS receiver module or a controlling PC. A particular
feature of the model to support this is that unknown input will cause it to
revert to its initial state.


State of Play
-------------

Currently the sequence of operations is:

* Write a Java class which builds up a StateModel.
* Create the necessary builders for the desired output.
* Pass them to the model's accept() method.

The builders are

* DiagramBuilder outputs a Graphviz .dot file to allow visualisation of the model.
* SimulatorBuilder creates a simulator designed to allow unit testing of the model.
* PicAsmBuilder, when written, will create the PIC assembler file. I expect to write three builders, for ASM, INC and H.

I have yet to achieve a passing unit test of a model. Maybe simple user error, maybe something more.

The Plan
--------

To make this more useful it would help if it could read a definition of the model and where and what to output from a text file. Simplest to code would likely be XML. This is more machine editable than human editable. Alternatively parse a simpler file with REGEX like expressions in it.

