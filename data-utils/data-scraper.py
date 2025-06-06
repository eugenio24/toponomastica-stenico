from bs4 import BeautifulSoup
import requests
from pyproj import Transformer
from pathlib import Path
import json

base_url = "https://www.cultura.trentino.it/Patrimonio-on-line/Dizionario-toponomastico-trentino/AreaVisitatore/"
comuni = ["Stenico"]

session = requests.Session()

def extract_hidden_fields(soup):
    hidden_inputs = {}
    for inp in soup.find_all("input", type="hidden"):
        if inp.get("name"):
            hidden_inputs[inp["name"]] = inp.get("value", "")
    return hidden_inputs

for comune in comuni:
    print(f"Processing: {comune}")
    search_url = base_url + "ResultsPage.aspx?Comuni=" + comune

    req = session.get(search_url)
    post_url = req.url
    soup = BeautifulSoup(req.text, 'html.parser')

    try:
        total_pages = int(soup.find(id="ctl00_ContentPlaceHolderForm_resultsList_totalPagesLabel").text.strip())
        current_page = int(soup.find(id="ctl00_ContentPlaceHolderForm_resultsList_currentPageLabel").text.strip())
    except:
        print(f"No results for {comune}")
        continue

    toponimi = []

    while current_page <= total_pages:
        print(f"Scraping page {current_page}/{total_pages} for {comune}...")

        entries = soup.find_all("div", class_="singolo-toponimo")

        for x in entries:
            detail_href = x.a.get('href')
            url = base_url + detail_href

            soup_detail = BeautifulSoup(requests.get(url).text, 'html.parser')

            nome_elem = soup_detail.find(id="ctl00_ContentPlaceHolderForm_FormaSimpleLabel")
            nome = nome_elem.text.strip() if nome_elem else None 

            forma_ufficiale_elem = soup_detail.find(id="ctl00_ContentPlaceHolderForm_FormaOfficialLabel")
            forma_ufficiale = forma_ufficiale_elem.text.strip() if forma_ufficiale_elem else None

            comune_elem = soup_detail.find(id="ctl00_ContentPlaceHolderForm_ComuneLabel")
            comune = comune_elem.text.strip() if comune_elem else None

            ind_geografico_elem = soup_detail.find(id="ctl00_ContentPlaceHolderForm_IndGeoLabel")
            ind_geografico = ind_geografico_elem.text.strip() if ind_geografico_elem else None

            descrizione_elem = soup_detail.find(id="ctl00_ContentPlaceHolderForm_DescrLabel")
            descrizione = descrizione_elem.text.strip() if descrizione_elem else None

            quota_elem = soup_detail.find(id="ctl00_ContentPlaceHolderForm_QuotaLabel")
            quota = quota_elem.text.strip() if quota_elem else None

            varianti_elem = soup_detail.find(id="ctl00_ContentPlaceHolderForm_VariantsGridView")
            if varianti_elem:
                varianti = []
                for row in varianti_elem.find_all("tr")[1:]:
                    cols = row.find_all("td")
                    if cols[0].text.strip():
                        varianti.append(cols[0].text.strip())
                    elif cols[2].text.strip():
                        varianti.append(cols[2].text.strip())
            else:
                varianti = None

            gis_elem = soup_detail.find(id="ctl00_ContentPlaceHolderForm_Gishttplink")

            if gis_elem:
                feature = gis_elem.get('href').split("feature=")[1]
                get_feature_url = f"https://webgis.provincia.tn.it/wgt/services/MapServer/getFeature?feature={feature}&geometryFormat=geojson&layer=toponimi"

                response = requests.get(get_feature_url)
                geojson = response.json()
                coords = geojson["results"]["geometry"]["coordinates"]

                if len(coords) > 1:
                    print(f"{nome} has {len(coords)} points")

                x, y = coords[0]
                transformer = Transformer.from_crs("EPSG:25832", "EPSG:4326", always_xy=True)
                lon, lat = transformer.transform(x, y)
                gis = [lon, lat]
            else:
                gis = None

            toponimo = {
                "id": len(toponimi),
                "nome": nome,
                "forma_ufficiale": forma_ufficiale,
                "comune": comune,
                "ind_geografico": ind_geografico,
                "descrizione": descrizione,
                "quota": quota,
                "varianti": varianti,
                "lat_long": gis
            }

            toponimi.append(toponimo)

        current_page += 1
        if current_page > total_pages:
            break

        next_button = soup.select_one('a.next[href*="__doPostBack"]')
        if not next_button:
            print("No next page button found.")
            break

        href = next_button.get("href", "")
        try:
            event_target = href.split("'")[1]   # 'ctl00$ContentPlaceHolderForm$resultsList$resultsDataPager$ctl03$ctl00'
        except IndexError:
            print("Invalid __doPostBack href format.")
            break

        hidden_fields = extract_hidden_fields(soup)
        hidden_fields['__EVENTTARGET'] = event_target
        hidden_fields['__EVENTARGUMENT'] = ''

        # set page
        hidden_fields["ctl00$ContentPlaceHolderForm$resultsList$resultsDataPager$ctl02$currentPageTxtBox"] = str(current_page-1)

        headers = {
            "Referer": post_url,
            "User-Agent": "Mozilla/5.0"
        }

        req = session.post(post_url, data=hidden_fields, headers=headers)
        post_url = req.url
        soup = BeautifulSoup(req.text, 'html.parser')

        try:
            current_page = int(soup.find(id="ctl00_ContentPlaceHolderForm_resultsList_currentPageLabel").text.strip())
            total_pages = int(soup.find(id="ctl00_ContentPlaceHolderForm_resultsList_totalPagesLabel").text.strip())
        except:
            print("Failed to read new page numbers after pagination.")
            break

    print(f"Found {len(toponimi)} toponimi for {comune}.")

    script_dir = Path(__file__).resolve().parent
    data_dir = script_dir.parent / "data" / "raw"
    data_dir.mkdir(parents=True, exist_ok=True)

    file_path = data_dir / f"{comune}.json"
    print(f"Saving to: {file_path.resolve()}")

    with open(file_path, "w", encoding="utf-8") as f:
        json.dump(toponimi, f, ensure_ascii=False, indent=2)
