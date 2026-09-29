#!/usr/bin/env python3
"""Check parity and count formatting for the Personal Arabic UI resources."""

from pathlib import Path
import re
import xml.etree.ElementTree as ET


RES = Path(__file__).resolve().parents[1] / "app/src/main/res"
PLACEHOLDER = re.compile(r"%(?:([1-9]\d*)\$)?([ds])")
ARABIC_QUANTITIES = {"zero", "one", "two", "few", "many", "other"}


def read(locale):
    elements = ET.parse(RES / locale / "strings.xml").getroot()
    return {
        element.attrib["name"]: element
        for element in elements
        if element.tag in {"string", "plurals"}
        and element.attrib.get("name", "").startswith(("flosi_", "validation_"))
    }


english = read("values")
arabic = read("values-ar")
assert english.keys() == arabic.keys(), (
    f"Missing Arabic: {sorted(english.keys() - arabic.keys())}; "
    f"missing English: {sorted(arabic.keys() - english.keys())}"
)

for name, source in english.items():
    translated = arabic[name]
    assert source.tag == translated.tag, f"Type mismatch: {name}"
    if source.tag == "string":
        assert sorted(PLACEHOLDER.findall(source.text or "")) == sorted(
            PLACEHOLDER.findall(translated.text or "")
        ), f"Format arguments differ: {name}"
    else:
        en_forms = {item.attrib["quantity"]: item for item in source}
        ar_forms = {item.attrib["quantity"]: item for item in translated}
        assert {"one", "other"} <= en_forms.keys(), f"English quantity forms missing: {name}"
        assert ARABIC_QUANTITIES <= ar_forms.keys(), f"Arabic quantity forms missing: {name}"
        source_args = set().union(*(set(PLACEHOLDER.findall(item.text or "")) for item in source))
        for quantity, item in ar_forms.items():
            assert set(PLACEHOLDER.findall(item.text or "")) <= source_args, (
                f"Unexpected format argument: {name}/{quantity}"
            )

print(f"Localization resources OK: {len(english)} paired strings/plurals")
