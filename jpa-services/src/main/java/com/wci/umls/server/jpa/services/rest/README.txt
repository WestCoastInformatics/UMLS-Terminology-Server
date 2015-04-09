This package is here instead of term-server-services because REST apis
require parameters that are implementations, thus the need for references
to Jpa classes which would not be in the classpath in the other project.