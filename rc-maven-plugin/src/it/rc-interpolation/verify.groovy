def properties = new Properties()
new File(basedir, "target/rc.properties").withReader { reader ->
    properties.load(reader);
    assert properties.get('test.c') == 'A.B';
}
