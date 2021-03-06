using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;
using TMPro;

using Photon.Pun;
using Photon.Realtime;

public class playerCtrl : MonoBehaviourPunCallbacks, IPunObservable
{
    // RigidBody와 CharacterController는 같이 사용하지 XX

    private float h, v, r;
    private Transform tr;
    private Transform[] trs;
    private Animator anim;
    public float moveSpeed = 20.0f;
    public float rotSpeed = 30.0f;
    float jumpSpeed = 10.0f;
    CharacterController cc;
    Vector3 playerPosition;
    float gravity = 100.0f;

    private Vector3 MoveDir;

    // animation trigger
    private bool isDance = false;
    private bool isSit = false;
    private bool isCry = false;

    public TextMeshPro nick;

    // Start is called before the first frame update
    void Start()
    {
        // child의 transform을 받아와야 함
        // GetComponentInChildren는 가장 첫 부모 포함 가장 처음 발견되는 component return
        // 즉, (CH_**)의 컴포넌트 return 따라서 GetComponentsInChildren 사용
        // [0]은 현재 gameobject (CH_**)의 transform
        // [1]이 Character의 transform
        trs = GetComponentsInChildren<Transform>();
        tr = trs[1];

        // Animator와 CharacterController는 child(Character)에만 있어서 GetComponentInChildren으로 받아올 수 있음
        anim = GetComponentInChildren<Animator>();
        cc = GetComponentInChildren<CharacterController>();

        MoveDir = Vector3.zero;
        
        // 각 플레이어 닉네임
        nick.text = photonView.Owner.NickName;
    }

    // Update is called once per frame
    void Update()
    {
        if (photonView.IsMine)
        {
            if (cc == null) return;

            h = Input.GetAxis("Horizontal");    // 좌우값
            v = Input.GetAxis("Vertical");      // 상하값

            // 이동 방향 설정
            Vector3 dir = new Vector3(h, 0, v);
            dir = dir.normalized;

            // 이동 방향에 따른 캐릭터 회전
            // Debug.Log("dir >> " + dir + " vector3  >>  " + Vector3.zero);
            if (dir != Vector3.zero)
            {
                float angle = Mathf.Atan2(dir.x, dir.z) * Mathf.Rad2Deg;
                if (Mathf.Abs(angle) == 90) angle *= -1f;
                else angle += 180f; 

                // 부드러운 회전
                tr.rotation = Quaternion.Slerp(tr.rotation, Quaternion.Euler(0, angle, 0), rotSpeed * Time.fixedDeltaTime);
            }

            // 움직임
            Movement();

            // 중력 적용
            // character controller에는 중력 자동으로 붙어있지 X
            // 따로 코드로 적용시켜줘야함
            h = Input.GetAxis("Horizontal");    // 좌우값
            v = Input.GetAxis("Vertical");      // 상하값

            dir = new Vector3(h, 0, v);
            dir = dir.normalized;
            dir.y -= gravity * Time.deltaTime*3;  // 중력

            dir = dir * moveSpeed * Time.deltaTime;
            cc.Move(dir * moveSpeed * Time.deltaTime);
        }
        else
        {
            // anim 적용
            tr.position = Vector3.Lerp(tr.position, currPos, Time.deltaTime * 7f);
            tr.rotation = Quaternion.Slerp(tr.rotation, currRot, Time.deltaTime * 7f);
            bool dance = currDance;
            bool cry = currCry;
            bool sit = currSit;

            if (currDance)
            {
                anim.SetTrigger("IsDancing");
                currDance = false;
            }
            if (currCry)
            {
                anim.SetTrigger("IsCrying");
                currCry = false;
            }
            if (currSit)
            {
                anim.SetTrigger("IsSitting");
                currSit = false;
            }

            if (tr.position != currPos) anim.SetBool("IsWalking", true);
            else anim.SetBool("IsWalking", false);
        }
    }

    private Vector3 currPos;
    private Quaternion currRot;
    private bool currDance = true;
    private bool currCry = true;
    private bool currSit = true;

    // 캐릭터 이동 & animation 유저 간 동기화
    public void OnPhotonSerializeView(PhotonStream stream, PhotonMessageInfo info)
    {
        if (stream.IsWriting)
        {
            // Sending Datas ...
            stream.SendNext(tr.position);
            stream.SendNext(tr.rotation);
            stream.SendNext(isDance);
            stream.SendNext(isCry);
            stream.SendNext(isSit);
            isDance = false;
            isCry = false;
            isSit = false;
        }
        else
        {
            currPos = (Vector3)stream.ReceiveNext();
            currRot = (Quaternion)stream.ReceiveNext();
            currDance = (bool)stream.ReceiveNext();
            currCry = (bool)stream.ReceiveNext();
            currSit = (bool)stream.ReceiveNext();
        }

    }

    // 캐릭터 조작
    public void Movement()
    {
        if(Input.GetKey(KeyCode.W) || Input.GetKey(KeyCode.UpArrow))
        {
            cc.Move(Vector3.back * moveSpeed * Time.deltaTime);
            anim.SetBool("IsWalking", true);
        }
        if (Input.GetKey(KeyCode.A) || Input.GetKey(KeyCode.LeftArrow))
        {
            cc.Move(Vector3.right * moveSpeed * Time.deltaTime);
            anim.SetBool("IsWalking", true);
        }
        if (Input.GetKey(KeyCode.S) || Input.GetKey(KeyCode.DownArrow))
        {
            cc.Move(Vector3.forward * moveSpeed * Time.deltaTime);
            anim.SetBool("IsWalking", true);
        }
        if (Input.GetKey(KeyCode.D) || Input.GetKey(KeyCode.RightArrow))
        {
            cc.Move(Vector3.left * moveSpeed * Time.deltaTime);
            anim.SetBool("IsWalking", true);
        }
        if (Input.GetKey(KeyCode.Alpha3))
        {
            anim.SetTrigger("IsSitting");
            isSit = true;
        }
        if (Input.GetKey(KeyCode.Alpha2))
        {
            anim.SetTrigger("IsDancing");
            isDance = true;
        }
        if (Input.GetKey(KeyCode.Alpha1))
        {
            anim.SetTrigger("IsCrying");
            isCry = true;
        }

        if (!Input.GetKey(KeyCode.W) && !Input.GetKey(KeyCode.S) && !Input.GetKey(KeyCode.D) && !Input.GetKey(KeyCode.A)
            && !Input.GetKey(KeyCode.UpArrow) && !Input.GetKey(KeyCode.LeftArrow) && !Input.GetKey(KeyCode.DownArrow) && !Input.GetKey(KeyCode.RightArrow))
            anim.SetBool("IsWalking", false);
    }

    private void Anim(float h, float v)
    {
        bool isWalking = (h != 0 || v != 0);
        Debug.Log("isWalking >>> " + isWalking);
        anim.SetBool("IsWalking", isWalking);
    }

    // CharacterController & RigidBody 간에 부딪힘 구현
    float pushPower = 2.0F;
    void OnControllerColliderHit(ControllerColliderHit hit)
    {
        Rigidbody body = hit.collider.attachedRigidbody;
        if (body == null || body.isKinematic)
            return;

        if (hit.moveDirection.y < -0.3F)
            return;

        Vector3 pushDir = new Vector3(hit.moveDirection.x, 0, hit.moveDirection.z);
        body.velocity = pushDir * pushPower;
    }
}
