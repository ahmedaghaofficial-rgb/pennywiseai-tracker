#!/usr/bin/env python3
"""Check parity, formatting and plural quantities for the Android UI resources."""

from pathlib import Path
from collections import Counter
import re
import xml.etree.ElementTree as ET


RES = Path(__file__).resolve().parents[1] / "app/src/main/res"
PLACEHOLDER = re.compile(r"%(?:([1-9]\d*)\$)?([ds])")
ARABIC_QUANTITIES = {"zero", "one", "two", "few", "many", "other"}


def read(locale):
    elements = ET.parse(RES / locale / "strings.xml").getroot()
    names = [element.attrib["name"] for element in elements if element.tag in {"string", "plurals"}]
    duplicates = [name for name, count in Counter(names).items() if count > 1]
    assert not duplicates, f"Duplicate {locale} resources: {duplicates}"
    return {
        element.attrib["name"]: element
        for element in elements
        if element.tag in {"string", "plurals"}
        and element.attrib.get("translatable") != "false"
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
        assert len(en_forms) == len(source), f"Duplicate English plural quantity: {name}"
        assert len(ar_forms) == len(translated), f"Duplicate Arabic plural quantity: {name}"
        assert {"one", "other"} <= en_forms.keys(), f"English quantity forms missing: {name}"
        assert ARABIC_QUANTITIES <= ar_forms.keys(), f"Arabic quantity forms missing: {name}"
        assert set(en_forms) <= ARABIC_QUANTITIES and set(ar_forms) == ARABIC_QUANTITIES, (
            f"Unexpected plural quantities: {name}"
        )
        source_args = set().union(*(set(PLACEHOLDER.findall(item.text or "")) for item in source))
        for quantity, item in ar_forms.items():
            assert set(PLACEHOLDER.findall(item.text or "")) <= source_args, (
                f"Unexpected format argument: {name}/{quantity}"
            )

loan_en = {item.attrib["quantity"]: item.text for item in english["flosi_active_loans"]}
loan_ar = {item.attrib["quantity"]: item.text for item in arabic["flosi_active_loans"]}
assert loan_en["one"] == "%1$d active loan"
assert loan_en["other"] == "%1$d active loans"
assert loan_ar["one"] != loan_ar["few"] and loan_ar["two"] != loan_ar["many"]

for name in (
    "flosi_onboarding_transactions_found",
    "flosi_onboarding_seconds_remaining",
    "flosi_onboarding_scan_saved",
    "flosi_move_profile_transactions_warning",
    "flosi_group_items",
    "flosi_group_transactions",
    "flosi_share_subscriptions_caption",
    "flosi_month_count",
    "flosi_year_count",
):
    en = {item.attrib["quantity"]: item.text for item in english[name]}
    ar = {item.attrib["quantity"]: item.text for item in arabic[name]}
    assert en["one"] != en["other"], f"English singular not distinct: {name}"
    assert ar["one"] != ar["few"] and ar["two"] != ar["many"], (
        f"Arabic quantity forms not distinct: {name}"
    )

print(f"Localization resources OK: {len(english)} paired strings/plurals")
