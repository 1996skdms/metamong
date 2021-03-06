using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;
using UnityEditor;

public class checkTowel : MonoBehaviour
{
    public const string BuiltinResources = "Resources/unity_builtin_extra";

    // Start is called before the first frame update
    void Start()
    {
        
    }

    // Update is called once per frame
    void Update()
    {
        // 클릭 감지
        if (Input.GetMouseButtonDown(0))
        {
            Ray ray = Camera.main.ScreenPointToRay(Input.mousePosition);

            RaycastHit hit = new RaycastHit();

            if (GetComponent<BoxCollider>().Raycast(ray, out hit, 10000f))
                clickTowel();
        }
    }

    public void clickTowel()
    {
        Debug.Log("수건 클릭했다!");
        GameObject clickedToggle = GameObject.Find("SecondToggle");
        Toggle t = clickedToggle.GetComponent(typeof(Toggle)) as Toggle;
        t.isOn = true;

        // 가이드 대화 추가
        GameObject canvas = GameObject.Find("Canvas");

        GameObject.Find("Canvas").transform.Find("Guide").gameObject.SetActive(true);
        GameObject guideText = GameObject.Find("Guide").transform.Find("Text").gameObject;
        Text pt = guideText.GetComponent(typeof(Text)) as Text;

        pt.text = "수건을 찾았다!\n컵을 찾아서 물로 수건을 적시자!";

        // 3초 후 Guide 숨기기
        Invoke("hideGuide", 3);

        // 3번째 미션 추가
        foreach (GameObject obj in GameObject.FindGameObjectsWithTag("Cup"))
        {
            obj.AddComponent<CheckCup>();
        }

        // 2번째 미션 삭제
        foreach (GameObject obj in GameObject.FindGameObjectsWithTag("Napkin"))
        {
            Destroy(obj.GetComponent<checkTowel>());
        }

    }

    public void hideGuide()
    {
        if (GameObject.Find("Guide") != null)
            GameObject.Find("Guide").SetActive(false);
    }
}
