def properties = new Properties()
new File(basedir, "module-2/target/classes/conf/project.properties").withReader { reader ->
    properties.load(reader);
    assert properties.get('test.yml.property') == 'default.yml.value';
    assert properties.get('test.yaml.property') == 'default.yaml.value';
    assert properties.get('test.json.property') == 'default.json.value';
    assert properties.get('test.properties.property') == 'env.properties.module-2.value';
}
