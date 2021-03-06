JModel Example
==============

The JModel example (`o.e.handly.examples.jmodel*`) demonstrates a Handly-based
model for the Java language along the lines of the JDT's Java model.

As one would expect from an example, the model is somewhat contrived.
In particular:

* Only classpath entries of kind `CPE_SOURCE`, with paths of length 1
are considered (i.e. only source folders that are direct children
of the project resource);

* Inclusion/exclusion filters are ignored -- everything is included;

* Only default output location is supported, with path of length 1;

* Initializers and local/anonymous types are not represented in the model.

Some of these constraints may be relaxed in the future (especially
based on community feedback).

Besides the core model and tests, the following UI parts, which are based on
the example model for Java, are provided:

* Example Java editor, with Handly-based reconciling and outline;

* Example Java navigator view (kindly contributed by Ondrej Ilcik, Codasip).

To try it out, launch runtime Eclipse and open the `JNavigator` view (in
the `Handly Examples` category). The view shows the elements of the underlying
example model for Java. You can navigate the structure of the projects in your
workspace down to types, methods, and fields in Java source files. Select
a Java file and edit it with the `Java Editor (Handly JModel Example)`.
See how changes to the inner structure of the source file are reflected
in both the JNavigator and the Outline view.

Please note that the UI functionality is necessarily very much contrived.
In particular, the example Java editor is based on a simple text editor
with no syntax coloring, content assist, or reporting problems as you type.
The goal has been to keep the example code comprehensible while demonstrating
the relevant Handly-related functionality such as reconciling and outline.
Similarly, the above-stated constraints of the example model are revealed
in earnest in the JNavigator view. Also, the view provides no actions for
manipulating the model -- you can use the standard Package Explorer for that --
but changes in the underlying model will, of course, be reflected in the view.

It is hoped that this example is sufficiently real-world despite
the inevitable simplifications vs. the full-fledged JDT implementation,
and will be useful to Handly adopters. Feedback is most welcome
and can be directed to the project's forum:

- <http://eclipse.org/forums/eclipse.handly>

or right to the developer mailing list:

- <https://dev.eclipse.org/mailman/listinfo/handly-dev>
