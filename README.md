def _section(keyword: str) -> str:
    headings = r"KÖK NEDEN|KOK NEDEN|HATA TİPİ|HATA TIPI|AÇIKLAMA|ACIKLAMA|ÖNERİ|ONERI|GÜVEN|GUVEN"
    pattern = rf"\*\*{re.escape(keyword)}\s*:?\s*\*\*(.+?)(?=\*\*(?:{headings})\s*:?\s*\*\*|\Z)"
    m = re.search(pattern, content, re.IGNORECASE | re.DOTALL)
    return m.group(1).strip() if m else ""
